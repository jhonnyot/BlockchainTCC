package br.uff.blockchain.model;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

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

	private String calculaHash() {
		sequencia++;
		return StringUtil.aplicaSha256(StringUtil.getStringFromKey(this.remetente)
				+ StringUtil.getStringFromKey(this.destinatario) + Float.toString(this.valor) + sequencia);
	}

	public void geraAssinatura(PrivateKey sk) {
		String dados = StringUtil.getStringFromKey(remetente) + StringUtil.getStringFromKey(destinatario)
				+ Float.toString(valor);
		this.assinatura = StringUtil.aplicaAssinaturaECDSA(sk, dados);
	}

	public boolean verificaAssinatura() {
		String dados = StringUtil.getStringFromKey(remetente) + StringUtil.getStringFromKey(destinatario)
				+ Float.toString(valor);
		return StringUtil.verificaAssinaturaECDSA(remetente, dados, assinatura);
	}

}
