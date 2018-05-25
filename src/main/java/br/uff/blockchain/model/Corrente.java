package br.uff.blockchain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import br.uff.blockchain.Blockchain;
import br.uff.blockchain.utility.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Corrente {

	private static Corrente instance;
	private static boolean GERA_TRANS;

	private List<Bloco> blockchain;
	private int dificuldade;
	private boolean modificada;

	private Corrente() {
		super();
		GERA_TRANS = false;
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

	public boolean mineraBloco(int dificuldade) {
		var b = new Bloco(this.blockchain.get(this.blockchain.size() - 1).getHash());
		if (GERA_TRANS) {
			Carteira remetente, destinatario;
			Random r = new Random();
			if (!(this.blockchain.size() > 1)) {
				remetente = Blockchain.c1;
				destinatario = Blockchain.c2;
			} else {
				boolean bool = r.nextBoolean();
				remetente = bool ? Blockchain.c1 : Blockchain.c2;
				destinatario = bool ? Blockchain.c2 : Blockchain.c1;
			}
			Transaction t = new Transaction(remetente.getPk(), destinatario.getPk(),
					(float) ThreadLocalRandom.current().nextDouble(0f, remetente.getSaldo()), new ArrayList<>());
			t.geraAssinatura(remetente.getSk());
			t.setId(StringUtil.aplicaSha256(t.toString()));
			t.getSaidas().add(new TransSaida(t.getDestinatario(), t.getValor(), t.getId()));
			t.getEntradas().add(new TransEntrada(t.getSaidas().get(0).getId()));
			b.addTransacao(t);
		}
		b.setRaizMerkle(StringUtil.getRaizMerkle(b.getDados()));
		b.validaHash(dificuldade);
		System.out.println(Thread.currentThread().getName() + ": Novo bloco encontrado: " + b.getHash());
		System.out.println("Nonces testados: " + (b.getAcc() * Bloco.TOTAL_NONCES));
		b.getNonces().clear();
		return Corrente.getInstance().adicionaBloco(b);
	}

	public synchronized boolean correnteValida() {
		Bloco atual;
		Bloco anterior;
		var hashAlvo = new String(new char[this.dificuldade]).replace("\0", "0");
		var tempUTXOs = new HashMap<String, TransSaida>();
		tempUTXOs.put(Blockchain.genesis.getSaidas().get(0).getId(), Blockchain.genesis.getSaidas().get(0));

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
				if (!atual.getHash().substring(0, this.dificuldade).equals(hashAlvo)) {
					System.out.println("Este bloco não foi minerado.");
					return false;
				}

				TransSaida tempSaida;
				for (var t = 0; t < atual.getDados().size(); t++) {
					Transaction transAtual = atual.getDados().get(t);
					if (!transAtual.verificaAssinatura()) {
						System.out.println("Assinatura da transação " + t + " é inválida.");
						return false;
					} else if (transAtual.getValoresEntradas() != transAtual.getValoresSaidas()) {
						System.out.println("Entradas não coincidem com saídas na transação " + t);
						return false;
					}
					for (TransEntrada e : transAtual.getEntradas()) {
						tempSaida = tempUTXOs.get(e.getTransSaidaId());
						if (tempSaida == null) {
							System.out.println("Transação referenciada na transação " + t + " não existe.");
							return false;
						} else if (e.getUTXO().getValor() != tempSaida.getValor()) {
							System.out.println("Valor inválido para transação referenciada em " + t);
							return false;
						}
						tempUTXOs.remove(e.getTransSaidaId());
					}
					for (TransSaida s : transAtual.getSaidas()) {
						tempUTXOs.put(s.getId(), s);
					}
					if (transAtual.getSaidas().get(0).getDestinatario() != transAtual.getDestinatario()) {
						System.out.println("Destinatário da transação " + t + " não é válido.");
						return false;
					} else if (transAtual.getSaidas().get(1).getDestinatario() != transAtual.getRemetente()) {
						System.out.println("Troco da transação " + t + " não vai para o remetente.");
						return false;
					}

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

	public void setFlagTeste() {
		GERA_TRANS = true;
	}

}
