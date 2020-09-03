package com.example.chatapp_nodejs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarException;

public class MessageAdapter extends RecyclerView.Adapter {

    private static final int TYPE_MESSAGE_SENT = 0;
    private static final int TYPE_MESSAGE_RECEIVED = 1;
    private static final int TYPE_IMAGE_SENT = 2;
    private static final int TYPE_IMAGE_RECEIVED = 3;

    private LayoutInflater inflater;
    private List<JSONObject> messages = new ArrayList<>();

    public MessageAdapter (LayoutInflater inflater) {
        this.inflater = inflater;
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        TextView messageTxt;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);

            messageTxt = itemView.findViewById(R.id.sentTxt);
        }
    }

    private class SentImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public SentImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView nameTxt, messageTxt;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.nameTxt);
            messageTxt = itemView.findViewById(R.id.receivedTxt);
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTxt;

        public ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            nameTxt = itemView.findViewById(R.id.nameTxt);

        }
    }

    @Override
    public int getItemViewType(int position) {

        JSONObject message = messages.get(position);

        try {
            if (message.getBoolean("isSent")) {

                if (message.has("message"))
                    return TYPE_MESSAGE_SENT;
                else
                    return TYPE_IMAGE_SENT;

            } else {

                if (message.has("message"))
                    return TYPE_MESSAGE_RECEIVED;
                else
                    return TYPE_IMAGE_RECEIVED;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType){

            case TYPE_MESSAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_message,parent,false);
                return new SentMessageHolder(view);

            case TYPE_MESSAGE_RECEIVED:
                view = inflater.inflate(R.layout.item_received_message,parent,false);
                return new ReceivedMessageHolder(view);

            case TYPE_IMAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_image,parent,false);
                return new SentImageHolder(view);

            case TYPE_IMAGE_RECEIVED:
                view = inflater.inflate(R.layout.item_received_photo,parent,false);
                return new ReceivedImageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        JSONObject message = messages.get(position);

        /**
         * if(message.getBoolean("isSent")) is True that means Message is sent so we have to work with
         * SentMessageHolder() or SentImageHolder().
         * if(message.getBoolean("isSent")) is Not True that means Message is Received so we have to work with
         * ReceivedMessageHolder() or ReceivedImageHolder().
         */

        try{
            if(message.getBoolean("isSent")){  /***Means Messages/Images Has Sent*/

                if (message.has("message")) { /***Means Message Has Sent*/

                    SentMessageHolder messageHolder = (SentMessageHolder) holder;
                    messageHolder.messageTxt.setText(message.getString("message"));

                }else{  /***Means Images Has Sent*/

                SentImageHolder imageHolder = (SentImageHolder) holder;
                    Bitmap bitmap = getBitmapFromString(message.getString("image"));

                    imageHolder.imageView.setImageBitmap(bitmap);
                
                }
            }else {  /**Means Messages/Images Has Received**/

            if(message.has("message")){ /***Means Messages Has Received*/

            ReceivedMessageHolder messageHolder = (ReceivedMessageHolder) holder;
            messageHolder.nameTxt.setText(message.getString("name"));
            messageHolder.messageTxt.setText(message.getString("message"));

            }else {  /***Means Image Has Received*/

            ReceivedImageHolder imageHolder = (ReceivedImageHolder) holder;
            imageHolder.nameTxt.setText(message.getString("name"));

            Bitmap bitmap = getBitmapFromString(message.getString("image"));
            imageHolder.imageView.setImageBitmap(bitmap);

            }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /***We Will Convert The Image String Into a BiteArray*/
    private Bitmap getBitmapFromString(String image) {

        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addItem(JSONObject jsonObject){
        messages.add(jsonObject);
        notifyDataSetChanged();
    }

}
