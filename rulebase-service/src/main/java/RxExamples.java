import com.google.common.collect.Lists;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static rx.schedulers.Schedulers.newThread;

/**
 * Created by saipkri on 17/08/17.
 */
public class RxExamples {

    public static void main(String[] args) {

        AtomicInteger i = new AtomicInteger(10);
        System.out.println(i.decrementAndGet());
        System.out.println(i);
    }

}
