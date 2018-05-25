package br.uff.blockchain.model;

import java.security.PublicKey;

import br.uff.blockchain.utility.StringUtil;

public class TransSaida {
	private String id;
	private PublicKey destinatario;
	private float valor;
	private String idTransacaoPai;

	public TransSaida(PublicKey destinatario, float valor, String idTransacaoPai) {
		this.setDestinatario(destinatario);
		this.setValor(valor);
		this.setIdTransacaoPai(idTransacaoPai);
		this.setId(StringUtil
				.aplicaSha256(StringUtil.getStringFromKey(destinatario) + Float.toString(valor) + idTransacaoPai));
	}

	public boolean isMine(PublicKey pk) {
		return (pk == this.destinatario);
	}

	public String getIdTransacaoPai() {
		return idTransacaoPai;
	}

	private void setIdTransacaoPai(String idTransacaoPai) {
		this.idTransacaoPai = idTransacaoPai;
	}

	public float getValor() {
		return valor;
	}

	private void setValor(float valor) {
		this.valor = valor;
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	public PublicKey getDestinatario() {
		return destinatario;
	}

	private void setDestinatario(PublicKey destinatario) {
		this.destinatario = destinatario;
	}
}
