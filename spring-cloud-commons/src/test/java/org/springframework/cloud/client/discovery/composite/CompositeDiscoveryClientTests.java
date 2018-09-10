/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client.discovery.composite;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientTestsConfig.CUSTOM_SERVICE_ID;

/**
 * Tests for behavior of Composite Discovery Client
 *
 * @author Biju Kunjummen
 */

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.application.name=service0",
		"spring.cloud.discovery.client.simple.instances.service1[0].uri=http://s1-1:8080",
		"spring.cloud.discovery.client.simple.instances.service1[1].uri=https://s1-2:8443",
		"spring.cloud.discovery.client.simple.instances.service2[0].uri=https://s2-1:8080",
		"spring.cloud.discovery.client.simple.instances.service2[1].uri=https://s2-2:443",}, classes = {
		CompositeDiscoveryClientTestsConfig.class})
public class CompositeDiscoveryClientTests {

	@Autowired
	private DiscoveryClient discoveryClient;

	@Test
	public void getInstancesByServiceIdShouldDelegateCall() {
		assertThat(this.discoveryClient).isInstanceOf(CompositeDiscoveryClient.class);

		assertThat(this.discoveryClient.getInstances("service1")).hasSize(2);

		ServiceInstance s1 = this.discoveryClient.getInstances("service1").get(0);
		assertThat(s1.getHost()).isEqualTo("s1-1");
		assertThat(s1.getPort()).isEqualTo(8080);
		assertThat(s1.getUri()).isEqualTo(URI.create("http://s1-1:8080"));
		assertThat(s1.isSecure()).isEqualTo(false);
	}

	@Test
	public void getServicesShouldAggregateAllServiceNames() {
		assertThat(this.discoveryClient.getServices()).containsOnlyOnce("service1", "service2", "custom");
	}

	@Test
	public void getDescriptionShouldBeComposite() {
		assertThat(this.discoveryClient.description()).isEqualTo("Composite Discovery Client");
	}

	@Test
	public void getInstancesShouldRespectOrder() {
		assertThat(this.discoveryClient.getInstances(CUSTOM_SERVICE_ID)).hasSize(1);
		assertThat(this.discoveryClient.getInstances(CUSTOM_SERVICE_ID)).hasSize(1);
	}

	@Test
	public void getInstancesByUnknownServiceIdShouldReturnAnEmptyList() {
		assertThat(this.discoveryClient.getInstances("unknown")).hasSize(0);
	}
}
