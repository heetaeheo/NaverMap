package com.project.navermap.presentation.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.navermap.data.entity.UserNameEntity
import com.project.navermap.data.repository.login.UserNameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userNameRepository: UserNameRepository
) : ViewModel() {

    var name : MutableLiveData<String> = MutableLiveData("")

    var userName : MutableLiveData<UserNameEntity> = MutableLiveData()

    fun saveData() = viewModelScope.launch(Dispatchers.IO){
        userNameRepository.saveUserName(
            UserNameEntity(
                userName = name.value!!
            )
        )
    }
    fun retrieveDate(){
        viewModelScope.launch(Dispatchers.IO) {
            userNameRepository.getUserName().collect{
                userName.postValue(it)
            }
        }
    }


}