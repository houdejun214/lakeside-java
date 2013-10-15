package com.lakeside.core.utils;

import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * utils for operate html doc
 * 
 * @author zhufb
 * 
 */
public class HtmlUtils {

	/**
	 * filter all tags and return text
	 * 
	 * @param s
	 * @return
	 */
	public static String filterTags(String html) {
		Element e = Jsoup.parse(html).select("body").first();
		return filterTags(e);
	}

	/**
	 * filter all tags ,return text
	 * 
	 * @param e
	 * @return
	 */
	public static String filterTags(Element e) {
		return e.text();
	}

	/**
	 * 
	 * 　filter all giving tags,all other tags remain
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterTags(String html, String tag) {
		Element e = Jsoup.parse(html).select("body").first();
		return filterTags(e,tag);
	}

	/**
	 * 
	 * 　filter all giving tags,all other tags remain
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterTags(Element e, String tag) {
		String[] tags = StringUtils.isEmpty(tag)?null:new String[]{tag};
		return filterTags(e, tags);
	}

	/**
	 * 
	 * 　filter all giving tags,all other tags remain
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterTags(String html, String[] tags) {
		Element e = Jsoup.parse(html).select("body").first();
		return filterTags(e,tags);
	}

	/**
	 * 
	 * 　filter all giving tags,all other tags remain
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterTags(Element e, String[] tags) {
		if(e == null){
			return null;
		}
		if(tags == null||tags.length == 0||!find(e,tags)) {
			return e.toString();
		}
		List<String> list = Arrays.asList(tags);
		StringBuilder result = new StringBuilder();
		List<Node> childNodes = e.childNodes();
		for (Node child : childNodes) {
			if (child instanceof Element ) {
				Element element = (Element) child;
				if(list.contains(element.tagName()))
					result.append(filterTags(element));
				else
					result.append(filterTags(element,tags));
			}else if(child instanceof TextNode){
				appendNormalisedText(result,e,(TextNode)child);
			}
		}
		return result.toString().trim();
	}

	/**
	 * 
	 * 　filter all tags without giving exclude Tag list
	 * 
	 * @param html
	 * @param excludeTags
	 * @return
	 */
	public static String filterExcludeTags(String html, String[] tags) {
		Element e = Jsoup.parse(html).select("body").first();
		return filterExcludeTags(e,tags);
	}

	/**
	 * 
	 * 　filter all tags without giving exclude Tag list
	 * 
	 * @param html
	 * @param excludeTags
	 * @return
	 */
	public static String filterExcludeTags(Element e, String[] tags) {
		if(e == null) {
			return null;
		}
		if (tags==null||tags.length == 0||!find(e,tags)) {
			return filterTags(e);
		}
		List<String> list = Arrays.asList(tags);
		StringBuilder result = new StringBuilder();
		appendWhitespaceIfBr(e, result);
		List<Node> childNodes = e.childNodes();
        for (Node child : childNodes) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                appendNormalisedText(result,e,textNode);
            } else if (child instanceof Element) {
                Element element = (Element) child;
                if (result.length() > 0 && element.isBlock() && !lastCharIsWhitespace(result))
                	result.append(" ");
                if(list.contains(element.tagName()))
                	result.append(element);
                else
                	result.append(filterExcludeTags(element,tags));
            }
        }
		return result.toString().trim();
	}

	/**
	 * 
	 * 　filter all tags without giving exclude Tag list
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterExcludeTags(String html, String tag) {
		Element e = Jsoup.parse(html).select("body").first();
		return filterExcludeTags(e,tag);
	}

	/**
	 *　filter all tags without giving exclude Tag list
	 * 
	 * @param html
	 * @param tags
	 * @return
	 */
	public static String filterExcludeTags(Element e, String tag) {
		String[] tags = StringUtils.isEmpty(tag)?null:new String[]{tag};
		return filterExcludeTags(e, tags);
	}

	static boolean find(Element doc, String[] tags) {
		StringBuffer selector = new StringBuffer();
		for(int i=0;i<tags.length;i++){
			selector.append(tags[i]).append(" ");
		}
		return find(doc,selector.toString());
	}
	
	static boolean find(Element doc, String selector) {
		String select = select(doc,selector);
		if (StringUtils.isEmpty(select)) {
			return false;
		}
		return true;
	}

	static String select(Element doc, String selector) {
		Element first = doc.select(selector).first();
		if (first == null) {
			return "";
		}
		String html = first.html();
		if (html == null) {
			html = "";
		}
		return html;
	}

	static void appendNormalisedText(StringBuilder accum, Element e,TextNode textNode) {
		String text = textNode.getWholeText();
		if (!preserveWhitespace(e)) {
			text = StringUtil.normaliseWhitespace(text);
			if (lastCharIsWhitespace(accum))
				text = stripLeadingWhitespace(text);
		}
		accum.append(text);
	}
	
	static boolean preserveWhitespace(Element e) {
        return e.tag().preserveWhitespace() || e.parent() != null && preserveWhitespace(e.parent());
	}

	static String stripLeadingWhitespace(String text) {
        return text.replaceFirst("^\\s+", "");
    }
    
	static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }
    
	static void appendWhitespaceIfBr(Element element, StringBuilder accum) {
        if (element.tag().getName().equals("br") && !lastCharIsWhitespace(accum))
            accum.append(" ");
    }
    
}
