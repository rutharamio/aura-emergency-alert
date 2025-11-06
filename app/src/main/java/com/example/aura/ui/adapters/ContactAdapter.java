package com.example.aura.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aura.R;
import com.example.aura.data.entities.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final List<Contact> contactList;

    // ===================== PASO 1: AÑADIR LISTENER =====================
    // Define una interfaz para que la Activity pueda "escuchar" el evento de clic largo.
    public interface OnItemLongClickListener {
        void onItemLongClick(Contact contact);
    }
    private OnItemLongClickListener longClickListener;

    // Método para que la Activity pueda registrarse como oyente.
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    // ====================================================================

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        // Usamos el método bind que ahora tiene la lógica del clic.
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // Se modifica la clase ViewHolder para que pueda manejar la lógica del clic
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvRelation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRelation = itemView.findViewById(R.id.tvRelation);
        }

        // ===================== PASO 2: MÉTODO BIND CON LÓGICA DE CLIC =====================
        // Este método asigna los datos y, lo más importante, el listener del clic largo.
        public void bind(final Contact contact) {
            // Asigna los datos a las vistas
            tvName.setText(contact.name);
            tvPhone.setText(contact.phone);
            tvRelation.setText(contact.relation);

            // Asigna el listener a la vista completa del item.
            itemView.setOnLongClickListener(view -> {
                // Si la Activity ha registrado un listener...
                if (longClickListener != null) {
                    // ...llamamos al método de la interfaz, pasándole el contacto que fue presionado.
                    longClickListener.onItemLongClick(contact);
                    return true; // Devuelve 'true' para indicar que el evento ha sido manejado.
                }
                return false;
            });
        }
        // =================================================================================
    }
}
