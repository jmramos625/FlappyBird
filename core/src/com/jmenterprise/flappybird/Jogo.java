package com.jmenterprise.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	//inserindo as imagens no App
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	//formas para colisão
	private ShapeRenderer shapeRenderer;
	private Circle circlePassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;


	//atributos de configurações
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = -1;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	//exibição de textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//configuração dos sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Objeto salvar pontuação
	Preferences preferencias;

	//Objetos para a camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;



	
	@Override
	public void create () {
		inicializarTexturas();

		inicializarObjetos();
	}

	@Override
	public void render () {

		//limpar os frame anteriores
		//usado para economizar recursos
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		validarPontos();
		verificarEstadoJogo();
		desenharTexturas();
		detectarColisoes();

	}

	private void verificarEstadoJogo(){

		//existem 3 estados possíveis
		//0 - Jogo inicial, passaro parado
		//1 - Começa o Jogo
		//2 - Colidiu

		boolean toqueTela =Gdx.input.justTouched();

		//verificando o estado
		if(estadoJogo == 0){
			//aplicando evento de click na tela
			if (toqueTela){
				//agora fazendo o pássaro voar
				gravidade = -15;
				//muda o estado do Jogo para a proxima fasw
				estadoJogo = 1;
				somVoando.play();
			}

		}else if (estadoJogo == 1){
			if (toqueTela){
				//agora fazendo o pássaro voar
				gravidade = -15;
				somVoando.play();
			}

			//movimentar o cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 400;

			//testar posição do cano para poder fazer a recriação
			if (posicaoCanoHorizontal < - canoBaixo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo - 20;
				posicaoCanoVertical = random.nextInt(800 )- 400;
				passouCano = false;
			}

			//aplicando gravidade no pássaro
			if (posicaoInicialVerticalPassaro > 15 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;


			gravidade++;


		}else if (estadoJogo == 2){
			//aplicando gravidade no pássaro
			/*if (posicaoInicialVerticalPassaro > 15 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;*/

			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);

			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if (toqueTela){
				//agora fazendo o pássaro voar
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanoHorizontal = larguraDispositivo;
			}

		}


	}

	private void detectarColisoes(){
		//para detectar as colisões é necessário desenhar as formas
		circlePassaro.set(10 + posicaoHorizontalPassaro+ passaros[0].getWidth()/2,posicaoInicialVerticalPassaro + passaros[0].getHeight()/2,passaros[0].getWidth()/2);
		retanguloCanoCima.set(posicaoCanoHorizontal,
				alturaDispositivo/2 + (espacoEntreCanos/2) + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());
		retanguloCanoBaixo.set(posicaoCanoHorizontal,
				(alturaDispositivo/2) - canoBaixo.getHeight() - (espacoEntreCanos/2) + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());


		//testando as colisões
		boolean colidiuCanoCima = Intersector.overlaps(circlePassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circlePassaro, retanguloCanoBaixo);

		if (colidiuCanoBaixo || colidiuCanoCima){
			Gdx.app.log("Log", "Colidiu com o cano");

			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}

		}

		/*
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		//renderer pássaro
		shapeRenderer.circle(10 + passaros[0].getWidth()/2,posicaoInicialVerticalPassaro + passaros[0].getHeight()/2,passaros[0].getWidth()/2);

		//renderer topo
		shapeRenderer.rect(posicaoCanoHorizontal,
				alturaDispositivo/2 + (espacoEntreCanos/2) + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());


		//rederer baixo
		shapeRenderer.rect(posicaoCanoHorizontal,
				(alturaDispositivo/2) - canoBaixo.getHeight() - (espacoEntreCanos/2) + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());

		shapeRenderer.setColor(Color.RED);

		shapeRenderer.end();
		 */


	}


	private void desenharTexturas(){
		//TODOS OS OBJETOS QUE SERÃO DESENHADOS ESTARÃO AQUI

		//para configurar o tamanho da tela
		batch.setProjectionMatrix(camera.combined);

		//faça o begin para determinar que está começando a renderizar uma imagem
		batch.begin();

		//definindo a textura de fundo
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);

		//método para desenhar imagens dentro do app
		batch.draw(passaros[(int) variacao], 10 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);

		//definindo a textura dos canos
		batch.draw(canoBaixo, posicaoCanoHorizontal,(alturaDispositivo/2) - canoBaixo.getHeight() - (espacoEntreCanos/2) + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo/2 + (espacoEntreCanos/2) + posicaoCanoVertical);

		//definindo a textura dos textos
		textoPontuacao.draw(batch,String.valueOf(pontos),larguraDispositivo/2, alturaDispositivo-110);

		//definindo a textura da pontuação
		if (estadoJogo == 2){
			//exibindo o game Over
			batch.draw(gameOver, (larguraDispositivo/2) - gameOver.getWidth()/2, alturaDispositivo/2 );
			//Reiniciando Jogo
			textoReiniciar.draw(batch,"Toque para Reiniciar!", larguraDispositivo/2 - 140, (alturaDispositivo/2) - gameOver.getHeight()/2);
			textoMelhorPontuacao.draw(batch,"Seu record é: "+ pontuacaoMaxima +" pontos", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight());
		}

		//finalizando o batch
		batch.end();
	}

	public void validarPontos(){
		//primeiro é necessário saber a posição horizontal do cano
		//com isso sempre que o cano passar pelo local do passaro, ele irá incrementar 1 ponto
		if (posicaoCanoVertical < 300){

			//passou da posição do pássaro
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();

			}

		}
		//movimentação do passaro
		variacao += Gdx.graphics.getDeltaTime() * 8;
		//primeiro é necessário incrementar só para depois verificar se está maior que 3 ou não
		//criar o variação para voltar ao 0 (bater asas)
		if (variacao > 3)
			variacao = 0;

	}


	private void inicializarTexturas(){
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");

		gameOver = new Texture("game_over.png");


	}

	private void inicializarObjetos(){
		//Gdx.app.log("create", "jogo iniciado");
		//faça a instanciação dos componentes criados
		batch = new SpriteBatch();

		//criando número random
		random = new Random();

		//chame os atributos de configuração
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;

		//posição pássaro
		posicaoInicialVerticalPassaro = alturaDispositivo/2;

		//posição canos
		posicaoCanoHorizontal = larguraDispositivo;

		//espaçamento dos canos
		espacoEntreCanos = 250;

		//configuração de textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		//formas geométricas para colisões
		shapeRenderer = new ShapeRenderer();
		circlePassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		//inicializar os sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//configurando preferência dos objetos
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);

		//configurando a câmera
		camera = new OrthographicCamera();
		//alterando a posição da camera para melhor visualização do jogo
		camera.position.set(VIRTUAL_WIDTH/2,VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}
}