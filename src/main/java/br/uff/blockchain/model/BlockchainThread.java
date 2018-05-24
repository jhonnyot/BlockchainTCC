package br.uff.blockchain.model;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BlockchainThread implements Runnable {

	private Thread t;						// Thread
	private String nome;					// Nome
	private int dificuldade;				// dificuldade do bloco a ser minerado
	private ArrayList<Transaction> dados;	// dados a serem adicionados ao bloco
	private int alvo;						// número de blocos a serem minerados pela thread
	private CyclicBarrier cb;				// barreira que garante que a execução será finalizada após a finalização
												// da(s) thread(s)

	public BlockchainThread(String nome, int dificuldade, ArrayList<Transaction> transacoes, int alvo,
			CyclicBarrier cb) {
		this.nome = nome;
		this.dificuldade = dificuldade;
		this.dados = transacoes;
		this.alvo = alvo;
		this.cb = cb;
	}

	@Override
	public void run() {
		System.out.println("Rodando " + this.nome);
		var alvo = Corrente.getInstance().getBlockchain().size() + this.alvo;
		try {
			while (Corrente.getInstance().getBlockchain().size() < alvo) {
				System.out.println(this.nome + ": Minerando bloco: " + Corrente.getInstance().getBlockchain().size());
				if (Corrente.getInstance().correnteValida()) {
					if (!Corrente.getInstance().mineraBloco(this.dificuldade, this.dados)) {
						continue;
					}
				} else continue;
			}
			System.out.println(this.nome + " finalizada.");
			this.cb.await();
		} catch (Exception e) {
			System.out.println(this.nome + " interrompida.");
			try {
				this.cb.await();
			} catch (InterruptedException | BrokenBarrierException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void start() {
		System.out.println("Iniciando thread " + this.nome + ".");
		if (this.t == null) {
			this.t = new Thread(this, nome);
			this.t.start();
		}
	}

}