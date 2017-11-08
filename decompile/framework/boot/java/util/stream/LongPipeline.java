package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfLong;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedLong;

public abstract class LongPipeline<E_IN> extends AbstractPipeline<E_IN, Long, LongStream> implements LongStream {

    public static abstract class StatelessOp<E_IN> extends LongPipeline<E_IN> {
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

    final /* synthetic */ class -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BiConsumer val$combiner;

        public /* synthetic */ -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(BiConsumer biConsumer) {
            this.val$combiner = biConsumer;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$combiner.accept(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl0 implements Supplier {
        public Object get() {
            return new LongSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl1 implements ObjLongConsumer {
        public void accept(Object arg0, long arg1) {
            ((LongSummaryStatistics) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((LongSummaryStatistics) arg0).combine((LongSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl0 implements Supplier {
        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl1 implements ObjLongConsumer {
        public void accept(Object arg0, long arg1) {
            LongPipeline.-java_util_stream_LongPipeline_lambda$9((long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            LongPipeline.-java_util_stream_LongPipeline_lambda$10((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalLong_max__LambdaImpl0 implements LongBinaryOperator {
        public long applyAsLong(long arg0, long arg1) {
            return Math.max(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalLong_min__LambdaImpl0 implements LongBinaryOperator {
        public long applyAsLong(long arg0, long arg1) {
            return Math.min(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0 implements LongConsumer {
        private /* synthetic */ Sink val$-lambdaCtx;

        public /* synthetic */ -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(Sink sink) {
            this.val$-lambdaCtx = sink;
        }

        public void accept(long arg0) {
            this.val$-lambdaCtx.accept(arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_LongStream_distinct__LambdaImpl0 implements ToLongFunction {
        public long applyAsLong(Object arg0) {
            return ((Long) arg0).longValue();
        }
    }

    final /* synthetic */ class -java_util_stream_Stream_boxed__LambdaImpl0 implements LongFunction {
        public Object apply(long arg0) {
            return Long.valueOf(arg0);
        }
    }

    final /* synthetic */ class -long__toArray__LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return new Long[arg0];
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements LongUnaryOperator {
        public long applyAsLong(long arg0) {
            return 1;
        }
    }

    final /* synthetic */ class -long_sum__LambdaImpl0 implements LongBinaryOperator {
        public long applyAsLong(long arg0, long arg1) {
            return Long.sum(arg0, arg1);
        }
    }

    public static class Head<E_IN> extends LongPipeline<E_IN> {
        public Head(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<Long> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Long> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(LongConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                LongPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        public void forEachOrdered(LongConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                LongPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends LongPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = (!StatefulOp.class.desiredAssertionStatus());

        public abstract <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Long[]> intFunction);

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

    LongPipeline(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    LongPipeline(Spliterator<Long> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    LongPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static LongConsumer adapt(Sink<Long> sink) {
        if (sink instanceof LongConsumer) {
            return (LongConsumer) sink;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using LongStream.adapt(Sink<Long> s)");
        }
        sink.getClass();
        return new -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(sink);
    }

    private static OfLong adapt(Spliterator<Long> s) {
        if (s instanceof OfLong) {
            return (OfLong) s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using LongStream.adapt(Spliterator<Long> s)");
        }
        throw new UnsupportedOperationException("LongStream.adapt(Spliterator<Long> s)");
    }

    public final StreamShape getOutputShape() {
        return StreamShape.LONG_VALUE;
    }

    public final <P_IN> Node<Long> evaluateToNode(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Long[]> intFunction) {
        return Nodes.collectLong(helper, spliterator, flattenTree);
    }

    public final <P_IN> Spliterator<Long> wrap(PipelineHelper<Long> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new LongWrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public final OfLong lazySpliterator(Supplier<? extends Spliterator<Long>> supplier) {
        return new OfLong(supplier);
    }

    public final void forEachWithCancel(Spliterator<Long> spliterator, Sink<Long> sink) {
        OfLong spl = adapt((Spliterator) spliterator);
        LongConsumer adaptedSink = adapt((Sink) sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Builder<Long> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Long[]> intFunction) {
        return Nodes.longBuilder(exactSizeIfKnown);
    }

    public final PrimitiveIterator.OfLong iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final OfLong spliterator() {
        return adapt(super.spliterator());
    }

    public final DoubleStream asDoubleStream() {
        return new java.util.stream.DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                return new ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.accept((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Long> boxed() {
        return mapToObj(new -java_util_stream_Stream_boxed__LambdaImpl0());
    }

    public final LongStream map(LongUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final LongUnaryOperator longUnaryOperator = mapper;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongUnaryOperator longUnaryOperator = longUnaryOperator;
                return new ChainedLong<Long>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longUnaryOperator.applyAsLong(t));
                    }
                };
            }
        };
    }

    public final <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final LongFunction<? extends U> longFunction = mapper;
        return new java.util.stream.ReferencePipeline.StatelessOp<Long, U>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<U> sink) {
                final LongFunction longFunction = longFunction;
                return new ChainedLong<U>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longFunction.apply(t));
                    }
                };
            }
        };
    }

    public final IntStream mapToInt(LongToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToIntFunction longToIntFunction = mapper;
        return new java.util.stream.IntPipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Integer> sink) {
                final LongToIntFunction longToIntFunction = longToIntFunction;
                return new ChainedLong<Integer>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longToIntFunction.applyAsInt(t));
                    }
                };
            }
        };
    }

    public final DoubleStream mapToDouble(LongToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToDoubleFunction longToDoubleFunction = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                final LongToDoubleFunction longToDoubleFunction = longToDoubleFunction;
                return new ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
    }

    public final LongStream flatMap(LongFunction<? extends LongStream> mapper) {
        final LongFunction<? extends LongStream> longFunction = mapper;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongFunction longFunction = longFunction;
                return new ChainedLong<Long>(sink) {

                    final /* synthetic */ class -void_accept_long_t_LambdaImpl0 implements LongConsumer {
                        private /* synthetic */ AnonymousClass1 val$this;

                        public /* synthetic */ -void_accept_long_t_LambdaImpl0(AnonymousClass1 anonymousClass1) {
                            this.val$this = anonymousClass1;
                        }

                        public void accept(long arg0) {
                            this.val$this.-java_util_stream_LongPipeline$6$1_lambda$3(arg0);
                        }
                    }

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(long t) {
                        Throwable th;
                        Throwable th2 = null;
                        LongStream longStream = null;
                        try {
                            longStream = (LongStream) longFunction.apply(t);
                            if (longStream != null) {
                                longStream.sequential().forEach(new -void_accept_long_t_LambdaImpl0());
                            }
                            if (longStream != null) {
                                try {
                                    longStream.close();
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
                        if (longStream != null) {
                            try {
                                longStream.close();
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

                    /* synthetic */ void -java_util_stream_LongPipeline$6$1_lambda$3(long i) {
                        this.downstream.accept(i);
                    }
                };
            }
        };
    }

    public LongStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public /* bridge */ /* synthetic */ LongStream sequential() {
        return (LongStream) sequential();
    }

    public /* bridge */ /* synthetic */ LongStream parallel() {
        return (LongStream) parallel();
    }

    public final LongStream filter(LongPredicate predicate) {
        Objects.requireNonNull(predicate);
        final LongPredicate longPredicate = predicate;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SIZED) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongPredicate longPredicate = longPredicate;
                return new ChainedLong<Long>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(long t) {
                        if (longPredicate.test(t)) {
                            this.downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    public final LongStream peek(LongConsumer action) {
        Objects.requireNonNull(action);
        final LongConsumer longConsumer = action;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, 0) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongConsumer longConsumer = longConsumer;
                return new ChainedLong<Long>(sink) {
                    public void accept(long t) {
                        longConsumer.accept(t);
                        this.downstream.accept(t);
                    }
                };
            }
        };
    }

    public final LongStream limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeLong(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final LongStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeLong(this, n, -1);
        }
    }

    public final LongStream sorted() {
        return SortedOps.makeLong(this);
    }

    public final LongStream distinct() {
        return boxed().distinct().mapToLong(new -java_util_stream_LongStream_distinct__LambdaImpl0());
    }

    public void forEach(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, false));
    }

    public void forEachOrdered(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, true));
    }

    public final long sum() {
        return reduce(0, new -long_sum__LambdaImpl0());
    }

    public final OptionalLong min() {
        return reduce(new -java_util_OptionalLong_min__LambdaImpl0());
    }

    public final OptionalLong max() {
        return reduce(new -java_util_OptionalLong_max__LambdaImpl0());
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new -java_util_OptionalDouble_average__LambdaImpl0(), new -java_util_OptionalDouble_average__LambdaImpl1(), new -java_util_OptionalDouble_average__LambdaImpl2());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void -java_util_stream_LongPipeline_lambda$9(long[] ll, long i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + i;
    }

    static /* synthetic */ void -java_util_stream_LongPipeline_lambda$10(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final long count() {
        return map(new -long_count__LambdaImpl0()).sum();
    }

    public final LongSummaryStatistics summaryStatistics() {
        return (LongSummaryStatistics) collect(new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl0(), new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl1(), new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl2());
    }

    public final long reduce(long identity, LongBinaryOperator op) {
        return ((Long) evaluate(ReduceOps.makeLong(identity, op))).longValue();
    }

    public final OptionalLong reduce(LongBinaryOperator op) {
        return (OptionalLong) evaluate(ReduceOps.makeLong(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeLong(supplier, accumulator, new -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(combiner)));
    }

    public final boolean anyMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.NONE))).booleanValue();
    }

    public final OptionalLong findFirst() {
        return (OptionalLong) evaluate(FindOps.makeLong(true));
    }

    public final OptionalLong findAny() {
        return (OptionalLong) evaluate(FindOps.makeLong(false));
    }

    public final long[] toArray() {
        return (long[]) Nodes.flattenLong((Node.OfLong) evaluateToArrayNode(new -long__toArray__LambdaImpl0())).asPrimitiveArray();
    }
}
