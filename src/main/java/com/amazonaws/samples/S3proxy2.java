package com.amazonaws.samples;

import java.util.Properties;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gaul.s3proxy.*;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.ContextBuilder;
import java.net.URI;

public class S3proxy2 {
	public static void main(String[]args) throws Exception{
		Properties properties = new Properties();
		properties.setProperty("jclouds.filesystem.basedir", "/tmp/blobstore");
	
		BlobStoreContext context = ContextBuilder
		        .newBuilder("filesystem")
		        .credentials("identity", "credential")
		        .overrides(properties)
		        .build(BlobStoreContext.class);
	
		S3Proxy s3Proxy = S3Proxy.builder()
		        .blobStore(context.getBlobStore())
		        .endpoint(URI.create("http://127.0.0.1:8081"))
		        .build();
	
		s3Proxy.start();
		while (!s3Proxy.getState().equals(AbstractLifeCycle.STARTED)) {
		    Thread.sleep(1);
		}
	}
}