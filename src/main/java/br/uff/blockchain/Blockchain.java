package br.uff.blockchain;

import java.security.Security;
import java.util.HashMap;
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
import br.uff.blockchain.model.TransSaida;
import br.uff.blockchain.model.Transaction;

public class Blockchain {

	public static final int DIFICULDADE = 3;
	public static final int NUMERO_THREADS = 2;
	public static final int MINIMO_TRANSACAO = 5;
	public static final ExecutorService exSvc = Executors.newFixedThreadPool(NUMERO_THREADS);
	public static final CyclicBarrier CB = new CyclicBarrier(NUMERO_THREADS + 1);

	public static HashMap<String, TransSaida> UTXOs = new HashMap<>();
	public static Transaction genesis;
	public static Carteira c1;
	public static Carteira c2;
	public static Carteira coinbase;

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		c1 = new Carteira();
		c2 = new Carteira();
		coinbase = new Carteira();
		genesis = new Transaction(coinbase.getPk(), c1.getPk(), 100f, null);
		genesis.geraAssinatura(coinbase.getSk());
		genesis.setId("0");
		genesis.getSaidas().add(new TransSaida(genesis.getDestinatario(), genesis.getValor(), genesis.getId()));
		UTXOs.put(genesis.getSaidas().get(0).getId(), genesis.getSaidas().get(0));
		var genBlock = new Bloco("0");
		genBlock.addTransacao(genesis);
		Corrente.getInstance().setDificuldade(DIFICULDADE);
		Corrente.getInstance().setFlagTeste();
		Corrente.getInstance().getBlockchain().add(genBlock);
		long inicio = System.nanoTime();
		for (var i = 1; i <= NUMERO_THREADS; i++) {
			var bt = new BlockchainThread("Thread " + i, DIFICULDADE, 10, CB);
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
