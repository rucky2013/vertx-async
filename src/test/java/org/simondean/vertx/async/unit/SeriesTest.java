package org.simondean.vertx.async.unit;

import org.junit.Test;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.ObjectWrapper;
import org.simondean.vertx.async.unit.fakes.FakeFailingAsyncSupplier;
import org.simondean.vertx.async.unit.fakes.FakeSuccessfulAsyncSupplier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesTest {
  @Test
  public void itStillExecutesWhenThereAreNoTasks() {
    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.series()
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        List<Object> resultList = result.result();
        assertThat(resultList).isNotNull();
        assertThat(resultList).isEmpty();
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }

  @Test
  public void itExecutesOneTask() {
    FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.series()
      .task(task1)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        List<Object> resultList = result.result();
        assertThat(resultList).isNotNull();
        assertThat(resultList).containsExactly(task1.result());
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }

  @Test
  public void itExecutesTwoTasks() {
    FakeSuccessfulAsyncSupplier<Object> task1 = new FakeSuccessfulAsyncSupplier<>("Task 1");
    FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.series()
      .task(task1)
      .task(task2)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isTrue();
        List<Object> resultList = result.result();
        assertThat(resultList).isNotNull();
        assertThat(resultList).containsExactly(task1.result(), task2.result());
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }

  @Test
  public void itFailsWhenATaskFails() {
    FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.series()
      .task(task1)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(task1.runCount()).isEqualTo(1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task1.cause());
        assertThat(result.result()).isNull();
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }

  @Test
  public void itExecutesNoMoreTasksWhenATaskFails() {
    FakeFailingAsyncSupplier<Object> task1 = new FakeFailingAsyncSupplier<>(new Throwable("Failed"));
    FakeSuccessfulAsyncSupplier<Object> task2 = new FakeSuccessfulAsyncSupplier<>("Task 2");

    ObjectWrapper<Integer> handlerCallCount = new ObjectWrapper<>(0);

    Async.series()
      .task(task1)
      .task(task2)
      .run(result -> {
        handlerCallCount.setObject(handlerCallCount.getObject() + 1);

        assertThat(result).isNotNull();
        assertThat(result.succeeded()).isFalse();
        assertThat(result.cause()).isEqualTo(task1.cause());
        assertThat(result.result()).isNull();
        assertThat(task1.runCount()).isEqualTo(1);
        assertThat(task2.runCount()).isEqualTo(0);
      });

    assertThat(handlerCallCount.getObject()).isEqualTo(1);
  }
}
