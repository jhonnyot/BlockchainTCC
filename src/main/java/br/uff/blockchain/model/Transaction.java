package br.uff.blockchain.model;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import br.uff.blockchain.Blockchain;
import br.uff.blockchain.utility.StringUtil;

public class Transaction {
	private String tID;
	private PublicKey remetente;
	private PublicKey destinatario;
	private float valor;
	private byte[] assinatura;

	private List<TransEntrada> entradas = new ArrayList<>();
	private List<TransSaida> saidas = new ArrayList<>();

	private static int sequencia = 0;

	public Transaction(PublicKey de, PublicKey para, float valor, ArrayList<TransEntrada> entradas) {
		this.remetente = de;
		this.destinatario = para;
		this.valor = valor;
		this.entradas = entradas;
	}

	public boolean processaTransacao() {
		if (!verificaAssinatura()) {
			System.out.println("A assinatura falhou na verificação.");
			return false;
		}

		for (TransEntrada ent : this.entradas) {
			ent.setUTXO(Blockchain.UTXOs.get(ent.getTransSaidaId()));
		}

		if (this.getValoresEntradas() < Blockchain.MINIMO_TRANSACAO) {
			System.out.println("Valor total das transações menor que o mínimo permitido.");
			System.out.println("Valor total: " + this.getValoresEntradas());
			System.out.println("Valor mínimo: " + Blockchain.MINIMO_TRANSACAO);
			return false;
		}
		var sobra = this.getValoresEntradas() - this.valor;
		this.tID = this.calculaHash();
		this.saidas.add(new TransSaida(this.destinatario, this.valor, this.tID));
		this.saidas.add(new TransSaida(this.remetente, sobra, this.tID));

		for (TransSaida s : this.saidas) {
			Blockchain.UTXOs.put(s.getId(), s);
		}

		for (TransEntrada e : this.entradas) {
			if (e.getUTXO() == null)
				continue;
			Blockchain.UTXOs.remove(e.getUTXO().getId());
		}

		return true;
	}

	public float getValoresEntradas() {
		var total = 0F;
		for (TransEntrada e : this.entradas) {
			if (e.getUTXO() == null)
				continue;
			total += e.getUTXO().getValor();
		}
		return total;
	}

	public float getValoresSaidas() {
		var total = 0F;
		for (TransSaida s : this.saidas) {
			total += s.getValor();
		}
		return total;
	}

	private String calculaHash() {
		sequencia++;
		return StringUtil.aplicaSha256(StringUtil.getStringFromKey(this.remetente)
				+ StringUtil.getStringFromKey(this.destinatario) + Float.toString(this.valor) + sequencia);
	}

	public void geraAssinatura(PrivateKey sk) {
		String dados = StringUtil.getStringFromKey(this.remetente) + StringUtil.getStringFromKey(this.destinatario)
				+ Float.toString(this.valor);
		this.assinatura = StringUtil.aplicaAssinaturaECDSA(sk, dados);
	}

	public boolean verificaAssinatura() {
		String dados = StringUtil.getStringFromKey(this.remetente) + StringUtil.getStringFromKey(this.destinatario)
				+ Float.toString(this.valor);
		return StringUtil.verificaAssinaturaECDSA(this.remetente, dados, this.assinatura);
	}

	public String getId() {
		return this.tID;
	}

	public void setId(String id) {
		this.tID = id;
	}

	public ArrayList<TransSaida> getSaidas() {
		return (ArrayList<TransSaida>) this.saidas;
	}

	public ArrayList<TransEntrada> getEntradas() {
		return (ArrayList<TransEntrada>) this.entradas;
	}

	public PublicKey getDestinatario() {
		return this.destinatario;
	}

	public PublicKey getRemetente() {
		return this.remetente;
	}

	public float getValor() {
		return this.valor;
	}

}
