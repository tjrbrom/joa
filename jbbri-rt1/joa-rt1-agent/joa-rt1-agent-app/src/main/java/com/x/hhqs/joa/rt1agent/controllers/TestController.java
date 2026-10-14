package com.x.hhqs.joa.rt1agent.controllers;
//package com.nsb.joa.rt1agent.controllers;
//
//import org.apache.dubbo.config.annotation.DubboReference;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.nsb.joa.rpc.service.DemoService;
//
//@RestController
//// @RequestMapping(value = "v1/joa")
//public class TestController {
//
//	//random, roundrobin, leastactive
//	//@DubboReference(check = false, version = "${demo.service.version}", loadbalance = "roundrobin")
//	@DubboReference(check = false, version = "${demo.service.version}")
//	private DemoService demoService;
//
//	@RequestMapping(value = "/test", method = RequestMethod.GET)
//	public String test() {
//		return demoService.sayHello("hg");
//	}
//
//}
