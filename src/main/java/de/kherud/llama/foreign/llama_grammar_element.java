package de.kherud.llama.foreign;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class llama_grammar_element extends Structure {
	/** @see LlamaLibrary.llama_gretype */
	public int type;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	/** Unicode code point or rule ID */
	public int value;
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public llama_grammar_element() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("type", "value");
	}
	public llama_grammar_element(int type, int value) {
		super();
		this.type = type;
		this.value = value;
	}
	public llama_grammar_element(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends llama_grammar_element implements Structure.ByReference {
		
	};
	public static class ByValue extends llama_grammar_element implements Structure.ByValue {
		
	};
}
