/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		// Spring容器使用的BeanFactory 是DefaultListableBeanFactory，它实现了BeanDefinitionRegistry接口，if条件成立。
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			/**
			 * BeanFactoryPostProcessor是一个顶级接口，他还有一个子类BeanDefinitionRegistryPostProcessor。
			 * 在该方法中声明了两个List来存放BeanFactoryPostProcessor和BeanDefinitionRegistryPostProcessor，以便控制这两个接口方法的执行。
			 */
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/**
			 * 遍历传入的List<BeanFactoryPostProcessor> beanFactoryPostProcessors，将其分类放到两个List中。
			 * 如果传入的是BeanDefinitionRegistryPostProcessor类，则先执行BeanDefinitionRegistryPostProcessor类中独有的方法postProcessBeanDefinitionRegistry方法。
			 */
			// beanFactoryPostProcessors是传进来的对象，把传入的对象分类放入 BeanFactoryPostProcessor 和 BeanDefinitionRegistryPostProcessor
			// public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor ， 是一个特殊的 BeanFactoryPostProcessor
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// 如果传入的 beanFactoryPostProcessor 是 BeanDefinitionRegistryPostProcessor 的子类
					// 则执行 org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry 中的方法
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.

			/**
			 * 第一次执行beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);方法，
			 * 从容器中获取BeanDefinitionRegistryPostProcessor类型的Bean的name（这里只是获取名称，还没有实例化Bean）。
			 * 注意，程序执行到这里，Spring还没有扫描包，还没有将项目中的Bean注册到容器中。
			 *
			 * 这个BeanDefinition是在什么时候被加入到BeanFactory的呢？
			 * 是在AnnotationConfigApplicationContext的无参构造器中创建reader时注册的BeanDefinition。
			 * 其中BeanName为org.springframework.context.annotation.internalConfigurationAnnotationProcessor，
			 * 对应的Class为org.springframework.context.annotation.ConfigurationClassPostProcessor。
			 */
			// 这里只能拿到spring内部的BeanDefinitionRegistryPostProcessor
			// 因为这里spring还没有去扫描Bean，获取不到通过@Component标识的自定义BeanDefinitionRegistryPostProcessor
			// 一般默认情况下，这里只有一个
			// BeanName：org.springframework.context.annotation.internalConfigurationAnnotationProcessor
			// BeanClass：ConfigurationClassPostProcessor
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			/**
			 * 遍历这个获取的postProcessorNames，
			 * 如果实现了PriorityOrdered接口，就调用beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class)方法，
			 * 从容器中获取这个Bean，将其加入到临时变量List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors中。
			 */
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// beanFactory.getBean() 这里开始创建 BeanDefinitionRegistryPostProcessor bean了
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}

			/**
			 * 对currentRegistryProcessors中的元素进行排序，然后执行BeanDefinitionRegistryPostProcessor中的特有方法postProcessBeanDefinitionRegistry。
			 * 注意，这里没有执行其父类的方法，而是又将其放到List<BeanDefinitionRegistryPostProcessor> registryProcessors中，到后面再执行其父类方法。
			 */
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);

			// registryProcessors 放的是 BeanDefinitionRegistryPostProcessor
			// 因为这里只执行BeanDefinitionRegistryPostProcessor中独有的方法，而不会执行其父类 即 BeanFactoryPostProcessor中的方法
			// 所以这里需要把处理器放入一个集合中，后续统一执行父类的方法
			registryProcessors.addAll(currentRegistryProcessors);

			// 执行 BeanDefinitionRegistryPostProcessor
			// currentRegistryProcessors 中放的是spring内部的 BeanDefinitionRegistryPostProcessor
			// 默认情况下,只有 org.springframework.context.annotation.ConfigurationClassPostProcessor
			// ConfigurationClassPostProcessor 里面就是在执行扫描Bean,并且注册BeanDefinition
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);

			/**
			 * 默认情况下，此时currentRegistryProcessors中只有一个Bean
			 * 即：org.springframework.context.annotation.ConfigurationClassPostProcessor（它实现了PriorityOrdered接口）。
			 *
			 * 当程序执行完ConfigurationClassPostProcessor的BeanDefinitionRegistryPostProcessor方法后，
			 * 我们程序中的Bean就被注册到了Spring容器中了
			 * 需要注意的是，这里还只是注册了BeanDefinition，还没有创建Bean对象。
			 */
			// 清空这个临时变量，方便后面再使用
			currentRegistryProcessors.clear();


			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.

			/**
			 * 当第二次执行postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);方法，
			 * 此时因为之前已经完成了Bean的扫描，所以如果我们有自定义的BeanDefinitionRegistryPostProcessor就可以在这里被获取了。
			 * 获取之前，判断其是否实现Ordered接口，并且之前没有被执行过，则调用getBean方法，
			 * 从容器中获取该Bean，然后进行排序，执行postProcessBeanDefinitionRegistry方法。
			 */
			// 这里已经可以获取到我们通过注册到Spring容器的 BeanDefinitionRegistryPostProcessor 了
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				// 之前优先处理的是实现PriorityOrdered接口的,而PriorityOrdered接口也实现了Ordered接口
				// 所以这里需要把之前已经处理过的给过滤掉
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					// 之前这个临时变量已经被清空了,现在又开始放东西了
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}

			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);

			registryProcessors.addAll(currentRegistryProcessors);

			// 执行BeanDefinitionRegistryPostProcessor
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);

			// 清空临时变量
			currentRegistryProcessors.clear();


			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.

			/**
			 * 前面已经按顺序执行了实现PriorityOrdered 和Ordered接口的BeanDefinitionRegistryPostProcessor，
			 * 最后，执行没有实现Ordered接口的BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry方法。
			 * 执行完之后再BeanDefinitionRegistryPostProcessor的父类方法postProcessBeanFactory。
			 */
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					// 执行没有实现Ordered接口的BeanDefinitionRegistryPostProcessor
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			// List<BeanDefinitionRegistryPostProcessor> registryProcessors
			// 之前已经执行过BeanDefinitionRegistryPostProcessor独有方法,现在执行其父类方法
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			// List<BeanFactoryPostProcessor> regularPostProcessors
			// 执行 BeanFactoryPostProcessor 方法
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		/**
		 * 获取容器中还没有被执行过的实现BeanFactoryPostProcessor接口的Bean，然后按顺序执行的postProcessBeanFactory。
		 */
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// 获取 BeanFactoryPostProcessor 的 beanName
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// 如果已经被执行过了,就不在执行
			// 因为一开始先获取的BeanDefinitionRegistryPostProcessor,而BeanDefinitionRegistryPostProcessor继承了BeanFactoryPostProcessor
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		// 根据不同的优先级,按序执行 BeanFactoryPostProcessor
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
