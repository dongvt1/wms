package org.jeecg.common.system.vo;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Dictionary
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictModel implements Serializable{
	private static final long serialVersionUID = 1L;

	public DictModel() {
	}
	
	public DictModel(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public DictModel(String value, String text, String color) {
		this.value = value;
		this.text = text;
		this.color = color;
	}

	/**
	 * dictionaryvalue
	 */
	private String value;
	/**
	 * dictionary文本
	 */
	private String text;
	/**
	 * dictionary颜色
	 */
	private String color;

	/**
	 * special purpose： JgEditableTable
	 * @return
	 */
	public String getTitle() {
		return this.text;
	}
	/**
	 * special purpose： vue3 Selectcomponents
	 */
	public String getLabel() {
		return this.text;
	}


	/**
	 * for form designer Associated record table data storage
	 * QQYUN-5595【form designer】Other table fields Import without translation
	 */
	private JSONObject jsonObject;

}
