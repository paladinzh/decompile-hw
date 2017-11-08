package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedInt;

public abstract class IntPipeline<E_IN> extends AbstractPipeline<E_IN, Integer, IntStream> implements IntStream {

    public static abstract class StatelessOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = (!StatelessOp.class.desiredAssertionStatus());

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled) {
                if ((upstream.getOutputShape() == inputShape ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    final /* synthetic */ class -int__toArray__LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return new Integer[arg0];
        }
    }

    final /* synthetic */ class -int_sum__LambdaImpl0 implements IntBinaryOperator {
        public int applyAsInt(int arg0, int arg1) {
            return Integer.sum(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BiConsumer val$combiner;

        public /* synthetic */ -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(BiConsumer biConsumer) {
            this.val$combiner = biConsumer;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$combiner.accept(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl0 implements Supplier {
        public Object get() {
            return new IntSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl1 implements ObjIntConsumer {
        public void accept(Object arg0, int arg1) {
            ((IntSummaryStatistics) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((IntSummaryStatistics) arg0).combine((IntSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl0 implements Supplier {
        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl1 implements ObjIntConsumer {
        public void accept(Object arg0, int arg1) {
            IntPipeline.-java_util_stream_IntPipeline_lambda$10((long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            IntPipeline.-java_util_stream_IntPipeline_lambda$11((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalInt_max__LambdaImpl0 implements IntBinaryOperator {
        public int applyAsInt(int arg0, int arg1) {
            return Math.max(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalInt_min__LambdaImpl0 implements IntBinaryOperator {
        public int applyAsInt(int arg0, int arg1) {
            return Math.min(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0 implements IntConsumer {
        private /* synthetic */ Sink val$-lambdaCtx;

        public /* synthetic */ -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(Sink sink) {
            this.val$-lambdaCtx = sink;
        }

        public void accept(int arg0) {
            this.val$-lambdaCtx.accept(arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_IntStream_distinct__LambdaImpl0 implements ToIntFunction {
        public int applyAsInt(Object arg0) {
            return ((Integer) arg0).intValue();
        }
    }

    final /* synthetic */ class -java_util_stream_Stream_boxed__LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return Integer.valueOf(arg0);
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements IntToLongFunction {
        public long applyAsLong(int arg0) {
            return 1;
        }
    }

    public static class Head<E_IN> extends IntPipeline<E_IN> {
        public Head(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(IntConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                IntPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        public void forEachOrdered(IntConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                IntPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = (!StatefulOp.class.desiredAssertionStatus());

        public abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> intFunction);

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled) {
                if ((upstream.getOutputShape() == inputShape ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    IntPipeline(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    IntPipeline(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    IntPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static IntConsumer adapt(Sink<Integer> sink) {
        if (sink instanceof IntConsumer) {
            return (IntConsumer) sink;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Sink<Integer> s)");
        }
        sink.getClass();
        return new -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(sink);
    }

    private static OfInt adapt(Spliterator<Integer> s) {
        if (s instanceof OfInt) {
            return (OfInt) s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Spliterator<Integer> s)");
        }
        throw new UnsupportedOperationException("IntStream.adapt(Spliterator<Integer> s)");
    }

    public final StreamShape getOutputShape() {
        return StreamShape.INT_VALUE;
    }

    public final <P_IN> Node<Integer> evaluateToNode(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Integer[]> intFunction) {
        return Nodes.collectInt(helper, spliterator, flattenTree);
    }

    public final <P_IN> Spliterator<Integer> wrap(PipelineHelper<Integer> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new IntWrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public final OfInt lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new OfInt(supplier);
    }

    public final void forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        OfInt spl = adapt((Spliterator) spliterator);
        IntConsumer adaptedSink = adapt((Sink) sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Builder<Integer> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Integer[]> intFunction) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }

    public final PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final OfInt spliterator() {
        return adapt(super.spliterator());
    }

    public final LongStream asLongStream() {
        return new java.util.stream.LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.accept((long) t);
                    }
                };
            }
        };
    }

    public final DoubleStream asDoubleStream() {
        return new java.util.stream.DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.accept((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Integer> boxed() {
        return mapToObj(new -java_util_stream_Stream_boxed__LambdaImpl0());
    }

    public final IntStream map(IntUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final IntUnaryOperator intUnaryOperator = mapper;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntUnaryOperator intUnaryOperator = intUnaryOperator;
                return new ChainedInt<Integer>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intUnaryOperator.applyAsInt(t));
                    }
                };
            }
        };
    }

    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final IntFunction<? extends U> intFunction = mapper;
        return new java.util.stream.ReferencePipeline.StatelessOp<Integer, U>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                final IntFunction intFunction = intFunction;
                return new ChainedInt<U>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intFunction.apply(t));
                    }
                };
            }
        };
    }

    public final LongStream mapToLong(IntToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToLongFunction intToLongFunction = mapper;
        return new java.util.stream.LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                final IntToLongFunction intToLongFunction = intToLongFunction;
                return new ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intToLongFunction.applyAsLong(t));
                    }
                };
            }
        };
    }

    public final DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToDoubleFunction intToDoubleFunction = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                final IntToDoubleFunction intToDoubleFunction = intToDoubleFunction;
                return new ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
    }

    public final IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        final IntFunction<? extends IntStream> intFunction = mapper;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntFunction intFunction = intFunction;
                return new ChainedInt<Integer>(sink) {

                    final /* synthetic */ class -void_accept_int_t_LambdaImpl0 implements IntConsumer {
                        private /* synthetic */ AnonymousClass1 val$this;

                        public /* synthetic */ -void_accept_int_t_LambdaImpl0(AnonymousClass1 anonymousClass1) {
                            this.val$this = anonymousClass1;
                        }

                        public void accept(int arg0) {
                            this.val$this.-java_util_stream_IntPipeline$7$1_lambda$3(arg0);
                        }
                    }

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(int t) {
                        Throwable th;
                        Throwable th2 = null;
                        IntStream intStream = null;
                        try {
                            intStream = (IntStream) intFunction.apply(t);
                            if (intStream != null) {
                                intStream.sequential().forEach(new -void_accept_int_t_LambdaImpl0());
                            }
                            if (intStream != null) {
                                try {
                                    intStream.close();
                                } catch (Throwable th3) {
                                    th2 = th3;
                                }
                            }
                            if (th2 != null) {
                                throw th2;
                            }
                            return;
                        } catch (Throwable th22) {
                            Throwable th4 = th22;
                            th22 = th;
                            th = th4;
                        }
                        if (intStream != null) {
                            try {
                                intStream.close();
                            } catch (Throwable th5) {
                                if (th22 == null) {
                                    th22 = th5;
                                } else if (th22 != th5) {
                                    th22.addSuppressed(th5);
                                }
                            }
                        }
                        if (th22 != null) {
                            throw th22;
                        }
                        throw th;
                    }

                    /* synthetic */ void -java_util_stream_IntPipeline$7$1_lambda$3(int i) {
                        this.downstream.accept(i);
                    }
                };
            }
        };
    }

    public /* bridge */ /* synthetic */ IntStream sequential() {
        return (IntStream) sequential();
    }

    public /* bridge */ /* synthetic */ IntStream parallel() {
        return (IntStream) parallel();
    }

    public IntStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public final IntStream filter(IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        final IntPredicate intPredicate = predicate;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntPredicate intPredicate = intPredicate;
                return new ChainedInt<Integer>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(int t) {
                        if (intPredicate.test(t)) {
                            this.downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    public final IntStream peek(IntConsumer action) {
        Objects.requireNonNull(action);
        final IntConsumer intConsumer = action;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, 0) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntConsumer intConsumer = intConsumer;
                return new ChainedInt<Integer>(sink) {
                    public void accept(int t) {
                        intConsumer.accept(t);
                        this.downstream.accept(t);
                    }
                };
            }
        };
    }

    public final IntStream limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeInt(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final IntStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeInt(this, n, -1);
        }
    }

    public final IntStream sorted() {
        return SortedOps.makeInt(this);
    }

    public final IntStream distinct() {
        return boxed().distinct().mapToInt(new -java_util_stream_IntStream_distinct__LambdaImpl0());
    }

    public void forEach(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, false));
    }

    public void forEachOrdered(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, true));
    }

    public final int sum() {
        return reduce(0, new -int_sum__LambdaImpl0());
    }

    public final OptionalInt min() {
        return reduce(new -java_util_OptionalInt_min__LambdaImpl0());
    }

    public final OptionalInt max() {
        return reduce(new -java_util_OptionalInt_max__LambdaImpl0());
    }

    public final long count() {
        return mapToLong(new -long_count__LambdaImpl0()).sum();
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new -java_util_OptionalDouble_average__LambdaImpl0(), new -java_util_OptionalDouble_average__LambdaImpl1(), new -java_util_OptionalDouble_average__LambdaImpl2());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void -java_util_stream_IntPipeline_lambda$10(long[] ll, int i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + ((long) i);
    }

    static /* synthetic */ void -java_util_stream_IntPipeline_lambda$11(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final IntSummaryStatistics summaryStatistics() {
        return (IntSummaryStatistics) collect(new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl0(), new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl1(), new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl2());
    }

    public final int reduce(int identity, IntBinaryOperator op) {
        return ((Integer) evaluate(ReduceOps.makeInt(identity, op))).intValue();
    }

    public final OptionalInt reduce(IntBinaryOperator op) {
        return (OptionalInt) evaluate(ReduceOps.makeInt(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeInt(supplier, accumulator, new -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(combiner)));
    }

    public final boolean anyMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.NONE))).booleanValue();
    }

    public final OptionalInt findFirst() {
        return (OptionalInt) evaluate(FindOps.makeInt(true));
    }

    public final OptionalInt findAny() {
        return (OptionalInt) evaluate(FindOps.makeInt(false));
    }

    public final int[] toArray() {
        return (int[]) Nodes.flattenInt((Node.OfInt) evaluateToArrayNode(new -int__toArray__LambdaImpl0())).asPrimitiveArray();
    }
}
