package br.uff.blockchain.model;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

public class Carteira {
	private PrivateKey sk;
	private PublicKey pk;

	public Carteira() {
		this.geraChaves();
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
