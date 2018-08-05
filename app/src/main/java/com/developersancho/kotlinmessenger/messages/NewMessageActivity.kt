package com.developersancho.kotlinmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.developersancho.kotlinmessenger.R
import com.developersancho.kotlinmessenger.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.row_user_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    companion object {
        val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/usersmsg")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null)
                        adapter.add(UserItem(user))
                }

                recyclerview_newmessage.adapter = adapter

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY,  userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()
                }
            }

        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.row_user_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.profile_textview_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.profile_imageview_new_message)
    }

}
