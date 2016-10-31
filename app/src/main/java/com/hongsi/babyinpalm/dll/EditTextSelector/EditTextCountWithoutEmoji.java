package com.hongsi.babyinpalm.dll.EditTextSelector;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/28.
 */

public class EditTextCountWithoutEmoji extends RelativeLayout {

    private Context mContext;
    private LayoutInflater mInflater;
    private EditText editText;
    private TextView showText;
    private int textSize;

    //输入表情前的光标位置
    private int cursorPos;
    //输入表情前EditText中的文本
    private String inputAfterText;
    //是否重置了EditText的内容
    private boolean resetText;

    public void setTEXT_MAX_SIZE(int TEXT_MAX_SIZE) {
        this.TEXT_MAX_SIZE = TEXT_MAX_SIZE;
    }

    private int TEXT_MAX_SIZE;    //最大的文字数目

    public EditTextCountWithoutEmoji(Context context) {
        super(context);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);

        mInflater.inflate(R.layout.edittext_withoutemoji_count,this);

        initView();
    }

    public EditTextCountWithoutEmoji(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);

        mInflater.inflate(R.layout.edittext_withoutemoji_count,this);

        initView();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.multi_text);
        showText = (TextView) findViewById(R.id.showText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!resetText) {
                    cursorPos = editText.getSelectionEnd();
                    // 这里用s.toString()而不直接用s是因为如果用s，
                    // 那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
                    // inputAfterText也就改变了，那么表情过滤就失败了
                    inputAfterText= s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!resetText) {
                    if (count - before>= 2) {//表情符号的字符长度最小为2
                        CharSequence input = s.subSequence(cursorPos, cursorPos + count);
                        if (containsEmoji(input.toString())) {
                            resetText = true;

                            //是表情符号就将文本还原为输入表情符号之前的内容
                            editText.setText(inputAfterText);
                            CharSequence text = editText.getText();
                            if (text instanceof Spannable) {
                                Spannable spanText = (Spannable) text;
                                Selection.setSelection(spanText, text.length());
                            }
                        }
                    }
                } else {
                    resetText = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                textSize = s.toString().length();

                //获取输入后的文字长度变化
                StringBuffer buffer = new StringBuffer();
                buffer.append(textSize);
                buffer.append("/");
                buffer.append(TEXT_MAX_SIZE);
                showText.setText(buffer.toString());

                if(textSize == TEXT_MAX_SIZE){
                    showText.setTextColor(getResources().getColor(R.color.red));;
                }else{
                    showText.setTextColor(getResources().getColor(R.color.blue));
                }
            }
        });
    }

    private boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }


    public boolean isEmoji(String string) {
        Pattern p = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        return m.find();
    }

    /**
     * 获取文本框的文字
     * @return
     */
    public String getText(){
        return editText.getText().toString();
    }

}
