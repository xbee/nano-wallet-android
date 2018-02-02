package co.nano.nanowallet.ui.receive;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sumimakito.awesomeqr.AwesomeQRCode;

import java.io.File;
import java.io.FileOutputStream;

import co.nano.nanowallet.R;
import co.nano.nanowallet.alarm.ClipboardAlarmReceiver;
import co.nano.nanowallet.databinding.FragmentReceiveBinding;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Settings main screen
 */
public class ReceiveDialogFragment extends BaseDialogFragment {
    private FragmentReceiveBinding binding;
    public static String TAG = ReceiveDialogFragment.class.getSimpleName();
    private static final int QRCODE_SIZE = 240;
    private static final String TEMP_FILE_NAME = "nanoreceive.png";
    private static final String ADDRESS_KEY = "co.nano.nanowallet.ui.receive.ReceiveDialogFragment.Address";
    private Address address;

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static ReceiveDialogFragment newInstance(Address address) {
        Bundle args = new Bundle();
        args.putSerializable(ADDRESS_KEY, address);
        ReceiveDialogFragment fragment = new ReceiveDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);

        address = (Address) getArguments().getSerializable(ADDRESS_KEY);

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_receive, container, false);
        View view = binding.getRoot();
        binding.setHandlers(new ClickHandlers());

        // colorize address text
        binding.receiveAddress.setText(UIUtil.getColorizedSpannable(address.getLongAddress(), getContext()));

        // generate QR code
        new AwesomeQRCode.Renderer()
                .contents(address.getLongAddress())
                .size((int) UIUtil.convertDpToPixel(QRCODE_SIZE, getContext()))
                .margin((int) UIUtil.convertDpToPixel(20, getContext()))
                .dotScale(0.55f)
                .background(BitmapFactory.decodeResource(getResources(), R.drawable.qrbackground))
                .renderAsync(new AwesomeQRCode.Callback() {
                    @Override
                    public void onRendered(AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
                        getActivity().runOnUiThread(() -> binding.receiveBarcode.setImageBitmap(bitmap));
                    }

                    @Override
                    public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
                        e.printStackTrace();
                    }
                });

        setStatusBarWhite(view);

        return view;
    }

    public Bitmap setViewToBitmapImage(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public void saveImage(Bitmap finalBitmap) {
        try {
            File cachePath = new File(getContext().getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(new File(cachePath + "/" + TEMP_FILE_NAME), true);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ClickHandlers {
        public void onClickClose(View view) {
            dismiss();
        }

        public void onClickShare(View view) {
            saveImage(setViewToBitmapImage(binding.receiveBarcode));
            File imagePath = new File(getContext().getCacheDir(), "images");
            File newFile = new File(imagePath, TEMP_FILE_NAME);
            Uri imageUri = FileProvider.getUriForFile(getContext(), "co.nano.nanowallet.fileprovider", newFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_TEXT, address.getLongAddress());
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setDataAndType(imageUri, getActivity().getContentResolver().getType(imageUri));
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.receive_share_title)));
        }

        public void onClickCopy(View view) {
            // copy address to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(ClipboardAlarmReceiver.CLIPBOARD_NAME, address.getLongAddress());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Snackbar snackbar = Snackbar.make(view, Html.fromHtml(getString(R.string.receive_copy_message)), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.receive_copy_done, view1 -> {
            });
            snackbar.show();

            setClearClipboardAlarm();
        }
    }
}
