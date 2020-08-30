package org.example.magazine;

import java.util.concurrent.Flow;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MagazineSubscriber implements Flow.Subscriber<Integer> { // Knowledge point 1: This is a #Subscriber#

    public static final String JACK = "Jack";
    public static final String PETE = "Pete";

    private final long sleepTime;
    private final String subscriberName;
    private Flow.Subscription subscription;// Knowledge point 2: This is a var for active request from publisher
    private int nextMagazineExpected;
    private int totalRead;

    MagazineSubscriber(final long sleepTime, final String subscriberName) {
        this.sleepTime = sleepTime;
        this.subscriberName = subscriberName;
        this.nextMagazineExpected = 1;
        this.totalRead = 0;
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);// Knowledge point 3: This is active request one item to get from publisher, do it in #onSubscribe#
    }

    @Override
    public void onNext(final Integer magazineNumber) {
        if (magazineNumber != nextMagazineExpected) {
            IntStream.range(nextMagazineExpected, magazineNumber).forEach(
                    msgNumber -> log("Oh no! I missed the magazine " + msgNumber)
            );
            // Catch up with the number to keep tracking missing ones
            nextMagazineExpected = magazineNumber;
        }
        log("Great! I got a new magazine: " + magazineNumber);
        takeSomeRest();
        nextMagazineExpected++;
        totalRead++;

        log("I'll get another magazine now, next one should be: " + nextMagazineExpected);
        subscription.request(1);// Knowledge point 4: This is active request one item to get from publisher, do it in #onNext#
    }

    @Override
    public void onError(final Throwable throwable) {
        log("Oops I got an error from the Publisher: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log("Finally! I completed the subscription, I got in total " + totalRead + " magazines.");
    }

    private void log(final String logMessage) {
        log.info("<=========== [{}] : {}", getSubscriberName(), logMessage);
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    private void takeSomeRest() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
