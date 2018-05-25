package br.uff.blockchain.model;

public class TransEntrada {
	private String transSaidaId;
	private TransSaida UTXO;

	public TransEntrada(String transSaidaId) {
		this.setTransSaidaId(transSaidaId);
	}

	public TransSaida getUTXO() {
		return UTXO;
	}

	public void setUTXO(TransSaida uTXO) {
		this.UTXO = uTXO;
	}

	public String getTransSaidaId() {
		return transSaidaId;
	}

	public void setTransSaidaId(String transSaidaId) {
		this.transSaidaId = transSaidaId;
	}
}
