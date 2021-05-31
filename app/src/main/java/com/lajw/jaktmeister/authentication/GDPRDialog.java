package com.lajw.jaktmeister.authentication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class GDPRDialog extends AppCompatDialogFragment {

    private String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In quis odio egestas mi volutpat venenatis nec ac metus. Phasellus non mi iaculis, ornare nibh ac, dapibus orci. Proin pretium iaculis convallis. Phasellus interdum aliquet ligula, a egestas dolor consequat vel. Nullam iaculis mattis scelerisque. Phasellus molestie porta consectetur. Proin fermentum rutrum dignissim. Pellentesque et risus sed libero commodo elementum. Quisque quis tempus risus, eget lobortis ante. Praesent accumsan mi neque. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque vitae ligula dolor. Ut egestas efficitur nisl, vitae maximus risus.\n" +
            "\n" +
            "Aenean tempor urna sed mollis ornare. Mauris eu enim euismod, egestas nibh hendrerit, finibus sapien. Curabitur varius efficitur elit id vestibulum. Nullam ut quam id erat rhoncus vulputate ut eget augue. Donec arcu lectus, tempus at mauris ac, tristique elementum leo. Pellentesque faucibus neque orci, id varius augue mattis non. Curabitur aliquam tincidunt nibh finibus tristique. Phasellus magna erat, egestas id lectus eget, gravida sodales nibh.\n" +
            "\n" +
            "Curabitur feugiat aliquet ultricies. Quisque dui urna, egestas sit amet purus non, vulputate tincidunt felis. Curabitur suscipit lacus ut orci lobortis sagittis. Mauris scelerisque turpis vitae nulla hendrerit rhoncus. Proin pretium nunc in nunc ultricies finibus. Aliquam posuere, urna a scelerisque efficitur, dolor enim semper risus, et interdum augue dolor in nibh. Nullam lacinia semper euismod. Aliquam tristique gravida velit, eu fermentum orci. Vivamus sagittis erat at mauris ultrices tristique. Mauris vitae justo magna. Nulla euismod turpis mauris, in posuere nulla consectetur nec.\n" +
            "\n" +
            "Duis pretium arcu sit amet elit finibus, a vulputate augue interdum. In aliquam quis nunc non vulputate. Nullam vulputate ipsum id eros gravida porta. Ut eleifend sem quam, eu rhoncus libero lacinia id. Suspendisse dictum, lacus non varius dignissim, urna orci malesuada nunc, quis convallis ex dui id massa. Phasellus eget lectus risus. Ut urna justo, auctor sit amet est vitae, fringilla convallis felis. Praesent a ligula odio. Mauris faucibus maximus tortor et accumsan. Curabitur tortor sem, pellentesque id mauris vel, cursus rhoncus leo. Curabitur cursus, risus vel euismod cursus, felis metus viverra neque, ac feugiat lacus massa sit amet purus. Praesent accumsan tempus turpis, quis consequat nulla luctus ut. Nullam at tortor egestas magna pulvinar volutpat.\n" +
            "\n" +
            "Praesent dapibus sem sed orci molestie, a hendrerit sapien eleifend. In sed nunc arcu. Morbi at semper ligula. Nunc accumsan, turpis et ornare accumsan, risus ante lacinia erat, fermentum tristique turpis sapien at sem. Nullam aliquam venenatis scelerisque. Curabitur cursus interdum dolor vel gravida. Sed eu mi quis enim tempus sagittis. Quisque pulvinar odio at nibh tristique, eu vestibulum lacus iaculis. Vestibulum quis est egestas, viverra lacus ut, porta nunc. Duis scelerisque, turpis ac imperdiet sodales, augue neque accumsan nulla, vel pretium mauris enim id massa. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Suspendisse sed lorem non velit feugiat efficitur. Phasellus dictum semper molestie. Fusce vitae ex a ex consectetur viverra sed sit amet neque. Integer iaculis, diam ut commodo rhoncus, urna arcu porttitor turpis, in congue sem massa nec erat.";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("GDPR")
                .setMessage(message)
                .setPositiveButton("Acceptera", (dialog, which) -> {

                });
        return builder.create();
    }
}
