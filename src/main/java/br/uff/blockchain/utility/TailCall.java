package br.uff.blockchain.utility;

import java.util.function.Supplier;

public abstract class TailCall<T> {
	public abstract TailCall<T> retomar();

	public abstract T eval();

	public abstract boolean isSuspenso();

	private TailCall() {
	}

	public static class Retorno<T> extends TailCall<T> {
		private final T t;

		public Retorno(T t) {
			this.t = t;
		}

		@Override
		public T eval() {
			return t;
		}

		@Override
		public boolean isSuspenso() {
			return false;
		}

		@Override
		public TailCall<T> retomar() {
			throw new IllegalStateException("Não é possível retomar um Retorno.");
		}
	}

	public static class Suspender<T> extends TailCall<T> {
		private final Supplier<TailCall<T>> retomar;

		public Suspender(Supplier<TailCall<T>> retomar) {
			this.retomar = retomar;
		}

		@Override
		public T eval() {
			TailCall<T> tailRec = this;
			while (tailRec.isSuspenso()) {
				tailRec = tailRec.retomar();
			}
			return tailRec.eval();
		}

		@Override
		public boolean isSuspenso() {
			return true;
		}

		@Override
		public TailCall<T> retomar() {
			return retomar.get();
		}
	}
}
