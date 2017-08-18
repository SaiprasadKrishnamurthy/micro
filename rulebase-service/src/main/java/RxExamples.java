import com.google.common.collect.Lists;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static rx.schedulers.Schedulers.newThread;

/**
 * Created by saipkri on 17/08/17.
 */
public class RxExamples {

    public static void main(String[] args) {

           /* Observable.fromCallable(thatReturnsNumberOne())
                    .map(doubleIt())
                    .map(doubleIt())
                    .subscribe(printResult());*/

            System.out.println(" ------------------- ");
            TestSubscriber<Object[]> testSubscriber = TestSubscriber.create(Observers.create(printResult()));

            Observable.fromCallable(thatReturnsNumberOne())
                    .flatMap(number -> {
                        Callable<Void> doubleIt = () -> foo(number);
                        Observable<Void> func1 = Observable.fromCallable(doubleIt).subscribeOn(newThread());
                        Observable<Void> func2 = Observable.fromCallable(doubleIt).subscribeOn(newThread());
                        return Observable.combineLatest(Lists.newArrayList(func1, func2), a -> a);
                    })
                    .map(f -> f)
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();


            /*System.out.println(" ------------------- ");
            Observable.fromCallable(thatReturnsNumberOne())
                    .map(doubleIt())
                    .observeOn(Schedulers.newThread())
                    .subscribe(printResult());
            System.out.println(" ------------------- ");
            Observable.fromCallable(thatReturnsNumberOne())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .map(doubleIt())
                    .observeOn(Schedulers.newThread())
                    .subscribe(printResult());*/
    }

    private static Action1<Object[]> printResult() {
        return result -> {
            System.out.println("Subscriber thread: " + Thread.currentThread().getName());
            System.out.println("Result: " + Arrays.deepToString(result));
        };
    }

    private static Func1<Integer, Void> doubleIt() {
        return number -> {
            sleepSeconds(1);
            System.out.println("\t\t[Doubling Function] Operator thread: " + Thread.currentThread().getName());
            return null;
        };
    }

    private static Void foo(final int i) {
        System.out.println("\t\t[Doubling Function] Operator thread: " + i + " -- " + Thread.currentThread().getName());
        sleepSeconds(1);
        return null; // Don't care
    }

    private static void sleepSeconds(int i) {
        try {
            Thread.sleep(1000 * i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Callable<Integer> thatReturnsNumberOne() {
        return () -> {
            System.out.println("Observable thread: " + Thread.currentThread().getName());
            return 1;
        };
    }

}
