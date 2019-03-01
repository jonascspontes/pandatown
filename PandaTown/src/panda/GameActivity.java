package panda;
import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

/*Activity do jogo que estende a SimpleBaseGameActivity da engine 
e implementa a interface IOnSceneTouchListener*/
public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener{

	//VARIÁVEIS
	//Tamanhos da tela
	private static final int LARGURA_CAMERA = 800;
	private static final int ALTURA_TELA = 480;
	private Camera camera;
	//Mapas de textura
	private BitmapTextureAtlas mapaTexturaBackground;
	private BitmapTextureAtlas mapaTexturaPanda;
	//Regions para carregamento dos assets de imagens
	private ITextureRegion camadaFinalBackground;
	private ITextureRegion camadaMeioBackground;
	private ITextureRegion camadaFrenteBackground;
	private TiledTextureRegion texturaBlocosPanda;
	//Objeto para Sprite animada
	private AnimatedSprite panda;
	//texturas sorvete
	private BitmapTextureAtlas mapaTexturaSorvete;
	private ITextureRegion texturaSorvete;
	private Sprite sorvete;
	//texturas pedra
	private BitmapTextureAtlas mapaTexturaPedra;
	private ITextureRegion texturaPedra;
	private Sprite pedra;
	//Música tema
	public Music musicaTema;
	//Som ao pontuar
	public Music soundCerto;
	//Som game over
	public Music soundErro;
	//variável de controle da movimentação do pensonagem
	private Boolean sobe = true;
	//Cena do jogo
	private Scene cena;
	//Controlador de Tempo
	private TimerHandler timer;
	//Variáveis para posicionamento inicial do sorvete
	private float sorveteY;
	private static final float FORA_TELA = -100;
	//Objetos do HUD
	private int pontos = 0;
	private HUD gameHUD;
	private Text pontosText;
	public Font font;
	//objetos do Game Over
	private Text gameOverText;

	//MÉTODOS
	//Método que cria a EngineOptions, configurando a câmera e demais opções da engine do jogo
	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, LARGURA_CAMERA, ALTURA_TELA);
		EngineOptions engine = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), camera); 
		engine.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
		return engine;
	}

	//Cria os recursos do jogo, disponibilizando os assets
	@Override
	protected void onCreateResources() {
		//carrega imagens
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		this.mapaTexturaPanda = new BitmapTextureAtlas(this.getTextureManager(), 256, 148, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.texturaBlocosPanda = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mapaTexturaPanda, this, "panda.png",0, 0, 3, 3);
		this.mapaTexturaPanda.load();

		mapaTexturaSorvete = new BitmapTextureAtlas(this.getTextureManager(),42,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.texturaSorvete = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mapaTexturaSorvete,this, "sorvete.png",0,0);
		this.mapaTexturaSorvete.load();

		mapaTexturaPedra = new BitmapTextureAtlas(this.getTextureManager(),128,64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.texturaPedra = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mapaTexturaPedra,this, "pedra.png",0,0);
		this.mapaTexturaPedra.load();

		this.mapaTexturaBackground = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		this.camadaFrenteBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mapaTexturaBackground, this, "front.png", 0, 0);
		this.camadaFinalBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mapaTexturaBackground, this, "back.png", 0, 392);
		this.camadaMeioBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mapaTexturaBackground, this, "nuvem.png", 0, 699);
		this.mapaTexturaBackground.load();
		sorveteY = ALTURA_TELA - this.texturaSorvete.getHeight() - 1;

		//Carrega fonte
		FontFactory.setAssetBasePath("font/");
		final ITexture mainFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Color color = new Color((85f/255f), (34f/255f), (0f),1f);
		int cor = color.getABGRPackedInt();
		font = FontFactory.createStrokeFromAsset(this.getFontManager(), mainFontTexture, this.getAssets(), "font.ttf", 32, true, cor, 1F, Color.BLACK_ABGR_PACKED_INT);
		font.load();
	}

	//Cria a cena e adiciona os itens a mesma
	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.cena = new Scene();
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		cena.setOnSceneTouchListener(this);

		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, ALTURA_TELA - this.camadaFinalBackground.getHeight(), this.camadaFinalBackground, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.camadaMeioBackground, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-15.0f, new Sprite(0, ALTURA_TELA - this.camadaFrenteBackground.getHeight(), this.camadaFrenteBackground, vertexBufferObjectManager)));

		cena.setBackground(autoParallaxBackground);

		final float playerX = (LARGURA_CAMERA - this.texturaBlocosPanda.getWidth()) / 2;
		final float playerY = ALTURA_TELA - this.texturaBlocosPanda.getHeight() - 5;

		this.panda = new AnimatedSprite(playerX, playerY, this.texturaBlocosPanda, vertexBufferObjectManager);
		panda.setScaleCenterY(this.texturaBlocosPanda.getHeight());
		panda.setScale(2);
		panda.animate(new long[]{100, 100, 100, 100, 100, 100, 100, 100, 100}, 0, 8, true);
		cena.attachChild(panda);

		this.sorvete = new Sprite(playerX + 400, sorveteY, this.texturaSorvete, vertexBufferObjectManager);
		this.sorvete.setScale(0.3F);

		this.sorvete.registerEntityModifier(new MoveXModifier(10, sorvete.getX(),FORA_TELA));
		cena.attachChild(sorvete);

		this.pedra = new Sprite(playerX + 800, playerY, this.texturaPedra, vertexBufferObjectManager);
		this.pedra.setScale(0.8F);

		this.pedra.registerEntityModifier(new MoveXModifier(10, pedra.getX(),FORA_TELA));
		cena.attachChild(pedra);


		//método externo para iniciar o handler responsável pelo timer do jogo
		iniciarTimeHandler();
		//registra na cena o handler responsável pelo timer do jogo
		cena.registerUpdateHandler(this.timer);

		//carrega música tema do jogo e sons
		try {
			musicaTema =  MusicFactory.createMusicFromAsset(this.getMusicManager(), this ,"mfx/tema.mp3");
			soundCerto = MusicFactory.createMusicFromAsset(this.getEngine().getMusicManager(), this ,"mfx/certo.wav");
			soundErro = MusicFactory.createMusicFromAsset(this.getEngine().getMusicManager(), this ,"mfx/errado.wav");

			musicaTema.setLooping(true);
			musicaTema.play();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//cria HUD de interface com usuário
		createHUD();

		return cena;
	}

	//Cria área de HUD para interface com usuário
	private void createHUD()
	{
		gameHUD = new HUD();
		//cria texto de pontuação 
		pontosText = new Text(250, 1, this.font, "0123456789", new TextOptions(HorizontalAlign.LEFT), getVertexBufferObjectManager());
		pontosText.setRotationCenter(0, 0); 
		pontosText.setText("PONTOS: 0");
		gameHUD.attachChild(pontosText);
		camera.setHUD(gameHUD);
	}
	/*inicia o timer que executará o método sobrescrito onTimePassed a cada 
	 * intervalo de milissegundos definido, além de permitir acompanhar 
	 * a contagem de tempo que será exibida na tela, verificar colisões, 
	 * atualizar pontuação e etc;
	 */
	private void iniciarTimeHandler() {

		this.timer = new TimerHandler(0.1f,true, new ITimerCallback(){
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {

				//verifica se houve colissão do sorvete com o panda
				if(sorvete.isVisible() && sorvete.collidesWith(panda)){
					//caso haja colissão aumenta os pontos para movimento e some o sorvete
					sorvete.clearEntityModifiers();
					sorvete.setVisible(false);
					addPontos();
				}

				//em caso de colisão com a pedra o jogo é finalizado
				if(pedra.isVisible() && pedra.collidesWith(panda)){
					soundErro.play();
					gameOver();
				}

				//adiciona sorvete em faixa contrária ao panda se estiver fora de tela
				if(sorvete.getX() < 0 || !sorvete.isVisible()){
					sorvete.setX(2000);
					if(sobe){
						sorvete.setY(panda.getY() - 140);	
					}else{
						sorvete.setY(panda.getY() + 40);
					}
					sorvete.setVisible(true);
					sorvete.registerEntityModifier(new MoveXModifier(15, sorvete.getX(),FORA_TELA));
				}


				//trata do posicionamento da pedra na faixa do player a cada ciclo
				if(pedra.getX() <= 0){
					pedra.setVisible(false);
					pedra.setX(panda.getX() + 800);
					if(sobe){
						pedra.setY(panda.getY());	
					}else{
						pedra.setY(panda.getY() - 30);
					}
					pedra.setVisible(true);
					pedra.registerEntityModifier(new MoveXModifier(10, pedra.getX(),FORA_TELA));
				}

			}

			//Exibe game over
			private void gameOver() {

				Rectangle retangulo = new Rectangle(0, 0, LARGURA_CAMERA, ALTURA_TELA, getVertexBufferObjectManager());
				retangulo.setColor(Color.WHITE);
				cena.attachChild(retangulo);
				gameOverText = new Text(0, 0, font, "GAME OVER", 20, new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
				gameOverText.setColor(Color.RED);
				gameOverText.setPosition((LARGURA_CAMERA - gameOverText.getWidth()) * 0.5f, (ALTURA_TELA - gameOverText.getHeight()) * 0.5f);
				gameOverText.registerEntityModifier(new ScaleModifier(2, 0.0f, 1.0f));
				cena.attachChild(gameOverText);
				musicaTema.stop();

			}

			//método para incrementar pontuação quando colide com sorvete
			private void addPontos() {
				pontos = pontos + 10;
				pontosText.setText("PONTOS: "+ pontos);
				soundCerto.play();
			}

		});

	}

	//Muda faixa onde o player está correndo
	private void mudaFaixa() {
		int y = -70;
		if(!sobe){
			y = y * -1;
			sobe = true;
		}else{
			sobe = false;
		}

		this.panda.setPosition(panda.getX(), panda.getY()+ y);
	}

	//Define o evento touch, disparado ao tocar na tela do dispositivo
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.isActionDown()) {
			this.mudaFaixa();
			return true;
		}
		return false;
	}

}
