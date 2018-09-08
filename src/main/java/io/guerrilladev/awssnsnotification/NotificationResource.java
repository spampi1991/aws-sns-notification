package io.guerrilladev.awssnsnotification;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class NotificationResource {

	final static Logger LOG = LoggerFactory.getLogger(NotificationResource.class);

	@RequestMapping("/test")
	public String test() {
		return "Hello World, i'm alive!";
	}

	@PostMapping("/notification")
	public void handleNotification(WebRequest request, @RequestBody Notification msg) throws MalformedURLException, IOException {
		String messageType = request.getHeader("x-amz-sns-message-type");
		LOG.info("SNS message type {}", messageType);
		if (messageType == null) {
			return;
		}
		//check message signature
		if (msg.getSignatureVersion().equals("1")) {
			if (isMessageSignatureValid(msg)) {
				LOG.info("Signature verification succeeded");
			} else {
				LOG.info("Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			LOG.info("Message signature {} not supported", msg.getSignatureVersion());
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}

		//process message based on type
		switch (messageType) {
			case "Notification":
				LOG.info("Notification received from topi {}", msg.getTopicArn());
				LOG.info("Subject: {} and Message: {}", msg.getSubject(), msg.getMessage());
				break;
			case "SubscriptionConfirmation":
				//You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics
				//that you want to enable to add this endpoint as a subscription.
				Scanner sc = new Scanner(new URL(msg.getSubscribeUrl()).openStream());
				StringBuilder sb = new StringBuilder();
				while (sc.hasNextLine()) {
					sb.append(sc.nextLine());
				}	LOG.info("Subscription confirmation ({}) Return value: {}", msg.getSubscribeUrl(), sb.toString());
				//Process the return value to ensure the endpoint is subscribed.
				break;
			case "UnsubscribeConfirmation":
				//Handle UnsubscribeConfirmation message.
				//For example, take action if unsubscribing should not have occurred.
				//You can read the SubscribeURL from this message and
				//re-subscribe the endpoint.
				LOG.info("Unsubscribe confirmation: {}", msg.getMessage());
				break;
			default:
				//Handle unknown message type.
				LOG.info("Unknown message type.");
				break;
		}
		LOG.info("Done processing message: {}", msg.getMessageId());
	}

	private boolean isMessageSignatureValid(Notification notification) {
		return true;
	}
}
