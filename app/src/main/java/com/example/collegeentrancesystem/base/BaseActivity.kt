package com.example.collegeentrancesystem.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Activity基类
 */
abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(), IGetPageName {

    protected lateinit var viewBinding: T
    protected abstract val inflater: (inflater: LayoutInflater) -> T
    private val compositeDisposable = CompositeDisposable()

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        // 这里可以添加页面打点
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        // 这里可以添加页面打点
    }

    @CallSuper
    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    /**
     * 添加Disposable
     */
    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
}