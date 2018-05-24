package br.uff.blockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.gson.GsonBuilder;

import br.uff.blockchain.model.BlockchainThread;
import br.uff.blockchain.model.Bloco;
import br.uff.blockchain.model.Carteira;
import br.uff.blockchain.model.Corrente;
import br.uff.blockchain.model.Transaction;
import br.uff.blockchain.utility.StringUtil;

public class Blockchain {

	public static final int DIFICULDADE = 5;
	public static Carteira c1;
	public static Carteira c2;
	public static final int NUMERO_THREADS = 15;
	public static final ExecutorService exSvc = Executors.newFixedThreadPool(NUMERO_THREADS);
	public static final CyclicBarrier CB = new CyclicBarrier(NUMERO_THREADS + 1);

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		c1 = new Carteira();
		c2 = new Carteira();
		System.out.println("Teste de Chaves:");
		System.out.println(StringUtil.getStringFromKey(c1.getSk()));
		System.out.println(StringUtil.getStringFromKey(c1.getPk()));
		Transaction t = new Transaction(c1.getPk(), c2.getPk(), 5, null);
		t.geraAssinatura(c1.getSk());
		System.out.println("Assinatura verificada: " + t.verificaAssinatura());
		var trans = new ArrayList<Transaction>();
		trans.add(t);
		Corrente.getInstance().setDificuldade(DIFICULDADE);
		Corrente.getInstance().getBlockchain().add(new Bloco(trans, "0"));
		long inicio = System.nanoTime();
		for (var i = 1; i <= NUMERO_THREADS; i++) {
			var bt = new BlockchainThread("Thread " + i, DIFICULDADE, trans, i, CB);
			exSvc.execute(bt);
		}
		try {
			CB.await();
		} catch (InterruptedException e) {
			System.out.println("Thread principal interrompida.");
		} catch (BrokenBarrierException e) {
			System.out.println("Barreira quebrada.");
		}
		long fim = System.nanoTime();
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(Corrente.getInstance().getBlockchain());
		System.out.println(json);
		System.out.println("Tempo de execução: " + TimeUnit.NANOSECONDS.toMillis(fim - inicio) + "ms.");
	}
}
