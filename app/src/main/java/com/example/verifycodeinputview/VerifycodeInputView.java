package com.example.verifycodeinputview;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.verifycodeinputview.DensityUtil;
import com.example.verifycodeinputview.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证码输入框
 * EditText字号极小，且颜色透明
 */

public class VerifycodeInputView extends FrameLayout {
    /**
     * 输入框个数
     */
    private int inputNum;
    /**
     * 输入框宽度
     */
    private int inputWidth;
    private int inputHeight;
    /**
     * 输入框之间的间隔
     */
    private int childPadding;
    /**
     * 输入框背景
     */
    private int editTextBg;
    /**
     * 文本颜色
     */
    private int textColor;
    /**
     * 文本字体大小
     */
    private int textSize;
    /**
     * 输入类型
     */
    private int inputType;




    public VerifycodeInputView(Context context) {
        this(context, null);
    }

    public VerifycodeInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifycodeInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inputNum = 6;
        inputWidth = DensityUtil.dip2px(context, 40);
        inputHeight = inputWidth;
        childPadding = DensityUtil.dip2px(context, 5f);
        textColor = getResources().getColor(R.color.color_333333);
        textSize = 24;
        editTextBg = R.drawable.verify_bg_gray;
        inputType = InputType.TYPE_CLASS_NUMBER;

        this.initViews();
    }

    private List<TextView> textViewList;
    private EditText editText;

    /**
     * 初始化view
     */
    private void initViews() {
        textViewList = new ArrayList<>(inputNum);
        LinearLayout llTextViewRoot = new LinearLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llTextViewRoot.setLayoutParams(layoutParams);
        llTextViewRoot.setOrientation(LinearLayout.HORIZONTAL);
        addView(llTextViewRoot);
        for (int i = 0; i < inputNum; i++) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(inputWidth, inputHeight);
            if (i != inputNum - 1) {//最后一个textView 不设置margin
                params.rightMargin = childPadding;
            }
            params.gravity = Gravity.CENTER;
            textView.setLayoutParams(params);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize);
            textView.setGravity(Gravity.CENTER);
            textView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            textView.setInputType(inputType);
            textView.setBackgroundResource(editTextBg);
            textView.setId(i);
            llTextViewRoot.addView(textView);
            textViewList.add(textView);
        }
        editText = new EditText(getContext());
        LayoutParams layoutParam2 = new LayoutParams(LayoutParams.MATCH_PARENT, inputHeight);
        editText.setLayoutParams(layoutParam2);
        editText.setTextSize(0.01f);
        //设置透明光标，如果直接不显示光标的话，长按粘贴会没效果
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editText, R.drawable.edit_cursor_bg_transparent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputNum)});
        editText.setInputType(inputType);
        editText.setTextColor(ContextCompat.getColor(getContext(), R.color.transparent));
        editText.setBackground(null);
        editText.addTextChangedListener(textWatcher);
        addView(editText);
        initListener();
    }

    private void initListener() {
        //屏蔽双击： 好多手机双击会出现 选择 剪切 粘贴 的选项卡，
        new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
        });
    }


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String inputContent = (null == editText.getText()) ? "" : editText.getText().toString();
            //已经有输入时，屏蔽长按和光标
            if (inputContent.length() > 0) {
                editText.setLongClickable(false);
                editText.setCursorVisible(false);
            } else {
                editText.setLongClickable(true);
                editText.setCursorVisible(true);
            }
            if (listener != null && inputContent.length() >= inputNum) {
                listener.onComplete(inputContent);
            }
            if (editListener != null){
                editListener.onEdit(inputContent);
            }
            for (int i = 0, len = textViewList.size(); i < len; i++) {
                TextView textView = textViewList.get(i);
                if (i < inputContent.length()) {

                    textView.setText(String.valueOf(inputContent.charAt(i)));
                } else {
                    textView.setText("");
                    textView.setBackgroundResource(R.drawable.verify_bg_gray);
                }
            }
        }
    };

    private boolean isAuto = false;

    /**
     * 设置宽高自适应，单个框的宽度平分父布局总宽度
     */
    public void setAutoWidth() {
        isAuto = true;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        if (isAuto && width != 0) {
            isAuto = false;
            resetWH(width);
        }
    }

    private void resetWH(int w) {
        int paddings = childPadding * (inputNum - 1);
        inputWidth = (w - paddings) / (inputNum);
        inputHeight = inputWidth;
        for (int i = 0, len = textViewList.size(); i < len; i++) {
            View child = textViewList.get(i);
            child.getLayoutParams().height = inputHeight;
            child.getLayoutParams().width = inputWidth;
        }
        editText.getLayoutParams().height = inputHeight;
    }

    /**
     * 获取编辑框内容
     *
     * @return 编辑框内容
     */
    public String getEditContent() {
        return editText.getText().toString();
    }

    /**
     * 清空输入框内容
     */

    public void clearEditContent(){
        editText.setText(null);
    }

    public OnCompleteListener listener;

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    public interface OnCompleteListener {
        /**
         * 完成验证码的填写
         *
         * @param content 填写内容
         */
        void onComplete(String content);
    }

    public OnEditListener editListener;
    public void setOnEditListener(OnEditListener editListener){
        this.editListener = editListener;
    }

    public interface OnEditListener{

        /**
         * 输入实时监听
         */
        void onEdit(String edit);
    }
}
