package br.uff.blockchain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Corrente {

	private static Corrente instance;

	private List<Bloco> blockchain;
	private int dificuldade;
	private boolean modificada;

	private Corrente() {
		super();
		List<Bloco> lista = Collections.synchronizedList(new ArrayList<>());
		ObservableList<Bloco> obsList = FXCollections.observableArrayList(lista);
		obsList.addListener(new ListChangeListener<Bloco>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Bloco> change) {
				Corrente.getInstance().setModificada(true);
				System.out.println(Thread.currentThread().getName() + ": Blockchain atualizada.");
			}
		});
		this.setBlockchain(obsList);
		this.setModificada(false);
	}

	public static synchronized Corrente getInstance() {
		if (instance == null) {
			instance = new Corrente();
		}
		return instance;
	}

	public synchronized List<Bloco> getBlockchain() {
		return blockchain;
	}

	private void setBlockchain(List<Bloco> blockchain) {
		this.blockchain = blockchain;
	}

	public synchronized boolean adicionaBloco(Bloco b) {
		if (this.correnteValida() && this.hashValido(b)) {
			this.getBlockchain().add(b);
			return true;
		} else {
			System.out.println("Tentativa inválida de adição na corrente.");
			return false;
		}
	}

	private boolean hashValido(Bloco b) {
		b.validaHash(this.dificuldade);
		return true;
	}

	public boolean mineraBloco(int dificuldade, ArrayList<Transaction> dados) {
		var b = new Bloco(dados, this.blockchain.get(this.blockchain.size() - 1).getHash());
		b.validaHash(dificuldade);
		System.out.println(Thread.currentThread().getName() + ": Novo bloco encontrado: " + b.getHash());
		System.out.println("Nonces testados: " + (b.getAcc() * Bloco.TOTAL_NONCES));
		b.getNonces().clear();
		return Corrente.getInstance().adicionaBloco(b);
	}

	public synchronized boolean correnteValida() {
		Bloco atual;
		Bloco anterior;

		if (this.isModificada()) {
			this.setModificada(false);
			return this.modificada;
		}

		if (this.blockchain.size() > 1) {
			for (var i = 1; i < this.blockchain.size(); i++) {
				atual = this.blockchain.get(i);
				anterior = this.blockchain.get(i - 1);
				if (!atual.getHash().equals(atual.calculaHash())) {
					System.out.println(Thread.currentThread().getName() + ": Hashes dos blocos não coincidem.");
					System.out.println(Thread.currentThread().getName() + ": " + atual.getHash());
					System.out.println(Thread.currentThread().getName() + ": " + atual.calculaHash());
					Corrente.getInstance().getBlockchain().remove(atual);
					return false;
				}
				if (!anterior.getHash().equals(atual.getHashAnterior())) {
					System.out.println(Thread.currentThread().getName() + ": Hash atual não coincide com o anterior.");
					System.out.println(Thread.currentThread().getName() + ": " + anterior.getHash());
					System.out.println(Thread.currentThread().getName() + ": " + atual.getHashAnterior());
					Corrente.getInstance().getBlockchain().remove(atual);
					return false;
				}
			}
		}
		return true;
	}

	public void setDificuldade(int dificuldade) {
		this.dificuldade = dificuldade;
	}

	public boolean isModificada() {
		return modificada;
	}

	private void setModificada(boolean modificada) {
		this.modificada = modificada;
	}

}
