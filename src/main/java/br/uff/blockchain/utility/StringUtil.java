package br.uff.blockchain.utility;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;

import br.uff.blockchain.model.Transaction;

public class StringUtil {

	public static String aplicaSha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");	// instancia algoritmo SHA-256
			byte[] hash = digest.digest(input.getBytes("UTF-8"));			// transforma a entrada em um vetor de bytes
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);			// transforma o byte em hash[i] em um
																				// número hexadecimal positivo
				if (hex.length() == 1) {									// caso o número possua apenas 1 dígito
					hexString.append('0');									// adiciona um 0
				}
				hexString.append(hex);										// adiciona o número à string
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] aplicaAssinaturaECDSA(PrivateKey sk, String entrada) {
		Signature dsa;
		byte[] saida = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(sk);
			byte[] strByte = entrada.getBytes();
			dsa.update(strByte);
			byte[] assinatura = dsa.sign();
			saida = assinatura;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return saida;
	}

	public static boolean verificaAssinaturaECDSA(PublicKey pk, String dados, byte[] assinatura) {
		try {
			Signature verificador = Signature.getInstance("ECDSA", "BC");
			verificador.initVerify(pk);
			verificador.update(dados.getBytes());
			return verificador.verify(assinatura);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getStringFromKey(Key chave) {
		return Base64.getEncoder().encodeToString(chave.getEncoded());
	}

	public static String getRaizMerkle(ArrayList<Transaction> trans) {
		var count = trans.size();
		var camadaAnterior = new ArrayList<String>();
		for (Transaction t : trans) {
			camadaAnterior.add(t.getId());
		}
		var camada = camadaAnterior;
		while (count > 1) {
			camada = new ArrayList<String>();
			for (var i = 1; i < camadaAnterior.size(); i++) {
				camada.add(aplicaSha256(camadaAnterior.get(i - 1) + camadaAnterior.get(i)));
			}
			count = camada.size();
			camadaAnterior = camada;
		}
		return (camada.size() == 1) ? camada.get(0) : "";
	}

}
