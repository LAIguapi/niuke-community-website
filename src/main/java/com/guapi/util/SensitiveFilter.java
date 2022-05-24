package com.guapi.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword=reader.readLine())!=null){
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode tempNode=rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            tempNode=subNode;
            //设置结束
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }

        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤完成
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针1
        TrieNode tempNode =rootNode;
        //指针2
        int begin=0;
        //指针3
        int position=0;
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c= text.charAt(position);
            //跳过符号
            if (isSymbol(c)) {
                //若指针1处于根节点，将此符号计入，指针2向下进行
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在中间或者开头，指针3都向下一步
                position++;
                continue;
            }
            tempNode=tempNode.getSubNode(c);
            if (tempNode == null) {
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //下一个位置
                position=++begin;
                //重新指向根节点
                tempNode=rootNode;
            } else if (tempNode.isKeywordEnd()) {
                //发现敏感词,将begin-position的字符串替换
                sb.append(REPLACEMENT);
                begin=++position;
                //重新指向根节点
                tempNode=rootNode;

            }else {
                //下一个
                position++;
            }
        }

        //将最后一批字符算入结果
        sb.append(text.substring(begin));
        return sb.toString();

    }
    private boolean isSymbol(Character character){
        // 0x2E80-0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(character)&&(character<0x2E80||character>0x9FFF);

    }

    //将敏感词添加到前缀树

    private class TrieNode {

        //关键词结束标识
        private boolean isKeywordEnd = false;

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        //子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点方法
        private void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }
}
