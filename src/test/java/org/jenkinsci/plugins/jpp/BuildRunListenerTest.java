package org.jenkinsci.plugins.jpp;


public class BuildRunListenerTest {

//	 @Test
//	 public void testAlexandriaListenerOnCompleteOfferElementIntoQueue() throws Exception {
//			BuildRunListener runListener = new BuildRunListener();
//			AsyncHttpRequestQueue asyncHttpQueue = new AsyncHttpRequestQueue();
//
//		 	AsyncHttpClientProxy mockedAsyncHttpClient = Mockito.mock(AsyncHttpClientProxy.class);
//		 	HttpPostHandler httpPostHandler = new HttpPostHandler(asyncHttpQueue, mockedAsyncHttpClient);
//		 	ExecutorService mockTaskExecutor = Mockito.mock(ExecutorService.class);
//		 	Mockito.doNothing().when(mockTaskExecutor).execute(httpPostHandler);
//
//		 	asyncHttpQueue.asyncHttpClientProxy = mockedAsyncHttpClient;
//		    asyncHttpQueue.httpPostHandler = httpPostHandler;
//		    asyncHttpQueue.taskExecutor = mockTaskExecutor;
//			asyncHttpQueue.startQueue(2);
//
//		 	runListener.jenkinsRootUrl = "jenkins_root_url";
//			runListener.overrideEnabledToTrue = true;
//		 	runListener.overrideAlexandriaUrl = "alexandria_url";
//		 	runListener.asyncHttpQueue = asyncHttpQueue;
//
//
//			Run mockedRun = Mockito.mock(Run.class);
//			Job mockedJob = Mockito.mock(Job.class);
//
//			HashMap<String, String> parameterNameValuePair = new HashMap<String, String>();
//			parameterNameValuePair.put("job_repository_url", "jenkins_root_url");
//			parameterNameValuePair.put("job_name", "job");
//			parameterNameValuePair.put("build_number", "0");
//
//		 	MessageBuilder requestData
//			 = new MessageBuilder(parameterNameValuePair, "alexandria_url/notify/test_results_available_for_build/submit");
//
//
//		 	Mockito.when(mockedRun.getParent()).thenReturn(mockedJob);
//			Mockito.when(mockedJob.getName()).thenReturn("job");
//			Mockito.when(mockedRun.getNumber()).thenReturn(0);
//
//			TaskListener mockedTastListener = Mockito.mock(TaskListener.class);
//		 	MessageBuilder rtnRequestData = runListener.createRequestData(mockedRun);
//
//			runListener.onCompleted(mockedRun, mockedTastListener);
//		 	Assert.assertEquals(1, asyncHttpQueue.blockQueue.size());
//		 	runListener.onCompleted(mockedRun, mockedTastListener);
//		 	Assert.assertEquals(2, asyncHttpQueue.blockQueue.size());
//		    runListener.onCompleted(mockedRun, mockedTastListener);
//		 	Assert.assertEquals(2, asyncHttpQueue.blockQueue.size());
//
//			Assert.assertEquals(rtnRequestData.getParameterNameValuePair(), requestData.getParameterNameValuePair());
//		 	Assert.assertEquals(rtnRequestData.getPostUrl(), requestData.getPostUrl());
//	}


}