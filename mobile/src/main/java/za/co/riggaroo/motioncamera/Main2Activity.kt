package za.co.riggaroo.motioncamera

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Main2Activity : AppCompatActivity() {
    private var mEmailField: EditText? = null
    private var mPasswordField: EditText? = null
    private var mSignIn: Button? = null
    private var mResult: TextView? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        //initialize the FirebaseAuth instance
        vaLue()
        mAuth = FirebaseAuth.getInstance()
        mSignIn!!.setOnClickListener { signIn(mEmailField!!.text.toString(), mPasswordField!!.text.toString()) }
    }

    private fun vaLue() {
        mEmailField = findViewById(R.id.field_email)
        mPasswordField = findViewById(R.id.field_password)
        mSignIn = findViewById(R.id.email_sign_in_button)
        mResult = findViewById(R.id.result)
    }

    //When initializing your Activity,
    // check to see if the user is currently signed in
    public override// [START on_start_check_user]
    fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly
        val currentUser = mAuth!!.currentUser
        // updateUI(currentUser);
    }
    // [END on_start_check_user]

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }
        // [START sign_in_with_email]
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        mResult!!.text = "Đăng nhập thành công"
                        val user = mAuth!!.currentUser
                        val myIntent = Intent(this@Main2Activity, MainActivity::class.java)
                        this@Main2Activity.startActivity(myIntent)
                        // updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        mResult!!.text = "Đăng nhập thất bại"
                        Toast.makeText(this@Main2Activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null);
                    }
                }
        // [END sign_in_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = mEmailField!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            mEmailField!!.error = "Required."
            valid = false
        } else {
            mEmailField!!.error = null
        }

        val password = mPasswordField!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            mPasswordField!!.error = "Required."
            valid = false
        } else {
            mPasswordField!!.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "EmailPassword"
    }

}
