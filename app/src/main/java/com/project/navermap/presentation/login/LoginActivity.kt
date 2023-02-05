package com.project.navermap.presentation.login

import android.app.Activity
import com.kakao.sdk.common.util.Utility
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Class
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.project.navermap.R
import com.project.navermap.databinding.ActivityLoginBinding
import com.project.navermap.presentation.mainActivity.MainActivity
import com.project.navermap.presentation.base.BaseActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {


    private var auth : FirebaseAuth? = null
    private var doubleBackToExit = false
    private var checkEye =0

    lateinit var mGoogleSignInClient : GoogleSignInClient
    lateinit var resultLauncher : ActivityResultLauncher<Intent>


    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       setResultSignUp()
        var keyHash = Utility.getKeyHash(this)
        Log.d("hash",keyHash)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        auth = FirebaseAuth.getInstance()
        binding.login.setOnClickListener {
            val email = binding.editId.text.toString()
            val password = binding.editPassword.text.toString()

            //Log.d("Login","Attept login with email/pw: $email/***")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{it ->
                    if(it.isSuccessful){
                        Toast.makeText(this,"위드마켓에 오신것을 환영합니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this,"이메일 및 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        val callback : (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
            } else if (token != null) {
                UserApiClient.instance.me { user, error ->
                    val kakaoId = user!!.id
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        binding.sign.setOnClickListener {
            registerMove()
        }

        binding.find.setOnClickListener {
            Sliding()
        }
        binding.sendReset.setOnClickListener {
            resetPassword()
        }
        binding.eye.setOnClickListener {
            showAndHide()
        }
        binding.google.setOnClickListener {
            val signIntent : Intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signIntent)
        }
        binding.kakao.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
                UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback = callback)
            }
        }
    }


    private fun setResultSignUp(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK){
                val task : Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignResult(task)
            } else{
                Toast.makeText(applicationContext,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignResult(completeTask: Task<GoogleSignInAccount>){
        try {
            val account = completeTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val photoUrl = account?.photoUrl.toString()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            Toast.makeText(this, "$email 님 로그인되었습니다", Toast.LENGTH_LONG).show()
        } catch (e : ApiException){

        }
    }

    override fun initViews() {
        super.initViews()



    }
    private fun showAndHide(){
        if(checkEye == 0){
            binding.editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.eye.setImageResource(R.drawable.eyes_show)
            checkEye = 1
        }else{
            binding.editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.eye.setImageResource(R.drawable.eyes)
            checkEye = 0
        }
    }


    private fun Sliding() {
        val slidePanel = binding.loginframe

        val state = slidePanel.panelState
        if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        } else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
    }

    override fun getViewBinding(): com.project.navermap.databinding.ActivityLoginBinding =
        com.project.navermap.databinding.ActivityLoginBinding.inflate(layoutInflater)

    override fun observeData() {}


    private fun registerMove(){
        val intent = Intent(this,SignUpActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (doubleBackToExit) {
            finishAffinity()
        } else {
            Toast.makeText(this, "종료하서려면 뒤로가기를 한번더 눌러주세요", Toast.LENGTH_SHORT).show()
            doubleBackToExit = true
            runDelayed(1500L) {
                doubleBackToExit = false
            }
        }
    }
    private fun runDelayed(millis: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, millis)
    }

    private fun resetPassword(){
        val email = binding.editReset.text.toString().trim()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(applicationContext,"이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
        }else{
            auth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this,"이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"메일이 정상적으로 보내지지않았습니다. 다시 시도해주세여", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}