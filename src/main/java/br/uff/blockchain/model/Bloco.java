package br.uff.blockchain.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import br.uff.blockchain.utility.StringUtil;
import br.uff.blockchain.utility.TailCall;

public class Bloco {
	private String hash; 													// hash do bloco atual
	private String hashAnterior; 											// hash do bloco anterior
	private ArrayList<Transaction> dados; 									// dados
	private long timeStamp;							 						// timestamp em ms
	private int nonce;														// nonce do bloco
	private List<Integer> nonces;											// Lista de nonces testados no bloco
	private int acc;														// Acumulador para manter o controle de
																			// nonces gerados
	private String raizMerkle;
	public static final int TOTAL_NONCES = (int) Math.pow(2, 16);			// Número máximo de nonces gerados

	// Construtor
	public Bloco(String hashAnterior) {
		this.hashAnterior = hashAnterior;
		this.setTimeStamp(new Date().getTime());
		this.dados = new ArrayList<>();
		this.nonce = 0;
		this.nonces = new ArrayList<>();
		this.raizMerkle = "";
		this.hash = this.calculaHash();
		this.setAcc(0);
	}

	public boolean addTransacao(Transaction trans) {
		if (trans == null)
			return false;
		if (this.hashAnterior != "0") {
			if (!trans.processaTransacao()) {
				System.out.println("Falha ao processar transação.");
				return false;
			}
		}
		this.dados.add(trans);
		System.out.println("Transação " + trans.getId() + " adicionada ao bloco.");
		return true;
	}

	public String calculaHash() {
		String hashCalculado = StringUtil.aplicaSha256(this.hashAnterior + Long.toString(timeStamp)
				+ this.dados.toString() + Integer.toString(nonce) + this.raizMerkle);
		return hashCalculado;
	}

	private TailCall<Integer> geraNonce() {
		if (this.acc == TOTAL_NONCES) {
			this.acc = 0;
			this.setTimeStamp(new Date().getTime());
			System.out.println(Thread.currentThread().getName()
					+ ": Número máximo de nonces excedidos para esta timestamp. Nova timestamp gerada: "
					+ this.timeStamp);
			return new TailCall.Suspender<>(() -> this.geraNonce());
		}
		var r = ThreadLocalRandom.current().nextInt(0, TOTAL_NONCES);
		this.acc++;
		// if (this.acc % 10000 == 0) {
		// System.out.println(Thread.currentThread().getName() + ": Testando nonce de
		// número " + this.acc);
		// System.out.println(Thread.currentThread().getName() + ": Nonce: " + r);
		// }
		return new TailCall.Retorno<>(r);
	}

	public void validaHash(int dificuldade) {
		String alvo = new String(new char[dificuldade]).replace("\0", "0");
		while (!hash.substring(0, dificuldade).equals(alvo)) {
			this.nonce = this.geraNonce().eval();
			this.hash = this.calculaHash();
		}
	}

	public ArrayList<Transaction> getDados() {
		return dados;
	}

	public void setDados(ArrayList<Transaction> dados) {
		this.dados = dados;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public synchronized String getHash() {
		return this.hash;
	}

	public synchronized String getHashAnterior() {
		return this.hashAnterior;
	}

	public void setRaizMerkle(String raiz) {
		this.raizMerkle = raiz;
	}

	public List<Integer> getNonces() {
		return this.nonces;
	}

	public int getAcc() {
		return acc;
	}

	private void setAcc(int acc) {
		this.acc = acc;
	}

}
