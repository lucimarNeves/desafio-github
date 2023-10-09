package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var  sharedPreferences: SharedPreferences

    var myDataSource: List<GitHubService> = ArrayList<GitHubService>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()
        getAllReposByUserName()

    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
      listaRepositories = findViewById(R.id.rv_lista_repositories)
      btnConfirmar = findViewById(R.id.btn_confirmar)
      nomeUsuario = findViewById(R.id.et_nome_usuario)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar

        btnConfirmar.setOnClickListener{

            saveUserLocal()


        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        sharedPreferences = getSharedPreferences ("repositorios", MODE_PRIVATE)

        val usuarioEdit = nomeUsuario.text.toString()
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_repos), usuarioEdit.toString())
            apply()
        }


        shareRepositoryLink(usuarioEdit.toString())
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
        val usuario = nomeUsuario.text.toString()
        sharedPreferences = getSharedPreferences(usuario, MODE_PRIVATE)

        if(sharedPreferences.contains(usuario)) {
            nomeUsuario.setText(sharedPreferences.getString(usuario, ""))
        }

    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */
        val baseUrl = "https://api.github.com/"
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build() //contruir
            githubApi = retrofit.create(GitHubService::class.java) //chamando api


    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {

        
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        val call = githubApi.getAllRepositoriesByUser("user")
        call.enqueue(object : Callback<List<Repository>> {
            //recuperando o valor através do retrofit
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {

                if (response.isSuccessful) {

                    response.body()?.let { //? pode ser nullable
                        setupAdapter(it)

                    }
                } else {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {

            }
        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios

         */
        val repAdapter = RepositoryAdapter(list)
        listaRepositories.apply {
            adapter = repAdapter

        }
        repAdapter.repoItemLister = { repos ->
           var  i = Intent(this, MainActivity::class.java)
            i.putExtra("Repositorios", nomeUsuario.text.toString())
            openBrowser(repos.htmlUrl)
        }

    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "repositorios")
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}