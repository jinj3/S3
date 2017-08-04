package com.amazonaws.samples;

import java.util.Properties;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gaul.s3proxy.*;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.ContextBuilder;
import java.net.URI;

/**
 * This class creates a mock S3 server for testing various calls without incurring
 * costs from AWS
 * @author jerryzhiruijin
 *
 */
public class S3proxy {
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
		        .endpoint(URI.create("http://127.0.0.1:8080"))
		        .build();
	
		s3Proxy.start();
		while (!s3Proxy.getState().equals(AbstractLifeCycle.STARTED)) {
		    Thread.sleep(1);
		}
	}
}
