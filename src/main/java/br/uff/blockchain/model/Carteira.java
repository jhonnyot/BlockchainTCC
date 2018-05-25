package br.uff.blockchain.model;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.uff.blockchain.Blockchain;

public class Carteira {
	private PrivateKey sk;
	private PublicKey pk;
	private HashMap<String, TransSaida> UTXOs;

	public Carteira() {
		this.geraChaves();
		this.UTXOs = new HashMap<>();
	}

	private void geraChaves() {
		try {
			KeyPairGenerator kg = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			kg.initialize(ecSpec, random);
			KeyPair kp = kg.generateKeyPair();
			this.setSk(kp.getPrivate());
			this.setPk(kp.getPublic());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float getSaldo() {
		var total = 0F;
		for (Map.Entry<String, TransSaida> item : Blockchain.UTXOs.entrySet()) {
			TransSaida UTXO = item.getValue();
			if (UTXO.isMine(this.pk)) {
				this.UTXOs.put(UTXO.getId(), UTXO);
				total += UTXO.getValor();
			}
		}
		return total;
	}

	public Transaction enviaFundos(PublicKey destinatario, float valor) {
		if (this.getSaldo() < valor) {
			System.out.println("Saldo insuficiente para realizar a transação. A transação foi cancelada.");
			return null;
		}
		var entradas = new ArrayList<TransEntrada>();

		var total = 0F;
		for (Map.Entry<String, TransSaida> s : this.UTXOs.entrySet()) {
			TransSaida UTXO = s.getValue();
			total += UTXO.getValor();
			entradas.add(new TransEntrada(UTXO.getId()));
			if (total > valor)
				break;
		}

		Transaction t = new Transaction(this.pk, destinatario, valor, entradas);
		t.geraAssinatura(sk);
		for (TransEntrada e : entradas) {
			this.UTXOs.remove(e.getTransSaidaId());
		}
		return t;
	}

	public PrivateKey getSk() {
		return sk;
	}

	private void setSk(PrivateKey sk) {
		this.sk = sk;
	}

	public PublicKey getPk() {
		return pk;
	}

	private void setPk(PublicKey pk) {
		this.pk = pk;
	}
}
