package com.mirzairwan.shopping;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.R.attr.visibility;

/**
 * Created by Mirza Irwan on 8/3/17.
 */

public class QuantityPicker
{
        private View mParentView;
        private View.OnClickListener mDefaultDownListener;
        private View.OnClickListener mDefaultUpListener;
        private TextView mTvHint;
        private TextView mTvError;
        private int mQuantity;
        private TextView mTvQty;
        private ImageButton mBtnUpCounter;
        private ImageButton mBtnDownCounter;

        public QuantityPicker(View view)
        {
                this(view, 1);
        }

        public QuantityPicker(View view, int initialQuantity)
        {
                mParentView = view;
                mBtnDownCounter = (ImageButton) view.findViewById(R.id.down_counter);
                mBtnUpCounter = (ImageButton) view.findViewById(R.id.up_counter);
                mTvQty = (TextView) view.findViewById(R.id.picker_item_quantity);
                mTvHint = (TextView) view.findViewById(R.id.hint_qty_picker);
                mTvError = (TextView) view.findViewById(R.id.error_qty_picker);
                mQuantity = initialQuantity;
                mTvQty.setText(String.valueOf(mQuantity));
                mDefaultUpListener = new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                setQuantity(++mQuantity);
                        }
                };

                mDefaultDownListener = new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                if (mQuantity > 1)
                                {
                                        setQuantity(--mQuantity);
                                }
                        }
                };

                setDefaultListeners();
        }

        public void setDefaultListeners()
        {
                mBtnDownCounter.setOnClickListener(mDefaultDownListener);
                mBtnUpCounter.setOnClickListener(mDefaultUpListener);
        }

        public void setOnClickListenerForUp(View.OnClickListener onClickListenerForUp)
        {
                mBtnUpCounter.setOnClickListener(onClickListenerForUp);
        }

        public void setOnClickListenerForDown(View.OnClickListener onClickListenerForDown)
        {
                mBtnDownCounter.setOnClickListener(onClickListenerForDown);
        }

        public void setQuantity(int quantity)
        {
                mQuantity = quantity;
                mTvQty.setText(String.valueOf(quantity));
        }

        public String getText()
        {
                return String.valueOf(mQuantity);
        }

        public void setError(String errorMsg)
        {
                mTvError.setText(errorMsg);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mBtnDownCounter.setOnTouchListener(onTouchListener);
                mBtnUpCounter.setOnTouchListener(onTouchListener);
        }

        public void setVisibility(int visibility)
        {
                mParentView.setVisibility(visibility);
                mTvQty.setVisibility(visibility);
                mBtnDownCounter.setVisibility(visibility);
                mBtnUpCounter.setVisibility(visibility);
                mTvHint.setVisibility(visibility);
        }

        public void setHint(String hint)
        {
                mTvHint.setText(hint);
        }

        public int getQuantity()
        {
                return mQuantity;
        }

        public void setErrorVisibility(int visibility)
        {
                mTvError.setVisibility(visibility);
        }

        public void setEnabled(boolean enabled)
        {
                mTvQty.setEnabled(enabled);
                mBtnDownCounter.setEnabled(enabled);
                mBtnUpCounter.setEnabled(enabled);
        }
}
