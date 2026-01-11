package com.example.pococ;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // 必须导入这个
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PocoListActivity  extends BaseActivity  {

    private TextView tvDate;
    private ImageView settingBtn;
    private ImageView btnDeleteCompleted;

    private TextView titleTextView;
    private TextView tvQuote;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private DatabaseHelper dbHelper;

    private ActivityResultLauncher<Intent> settingsLauncher;

    private final String[] TAB_TITLES = {"日计划", "月计划", "季计划", "年计划"};

    private Handler quoteHandler = new Handler(Looper.getMainLooper());
    private Runnable quoteRunnable;
    private int currentQuoteIndex = 0;

    private final String[] quotes = {
            "世上无难事，只要肯登攀。",
            "将来的你，一定会感谢现在拼命的自己。",
            "不积跬步，无以至千里；不积小流，无以成江海。",
            "含泪播种的人一定能含笑收获。",
            "只有极致的拼搏，才能配得上极致的风景。",
            "每一个不曾起舞的日子，都是对生命的辜负。",
            "星光不问赶路人，时光不负有心人。",
            "你若盛开，蝴蝶自来；你若精彩，天自安排。",
            "生活从来不会亏待每一个努力向上的人。",
            "与其抱怨黑暗，不如提灯前行。",
            "没有白走的路，每一步都算数。",
            "在这个世界上，没有偶然，只有必然。",
            "坚持不懈，直到成功。",
            "昨晚多几分钟的准备，今天少几小时的麻烦。",
            "行动是治愈恐惧的良药，而犹豫拖延将不断滋养恐惧。",
            "如果你想攀登高峰，切莫把彩虹当作梯子。",
            "成功的秘诀在于永不改变既定的目的。",
            "伟大的作品，不是靠力量而是靠坚持才完成的。",
            "无论你觉得自己多么的不幸，永远有人比你更加不幸。",
            "无论你觉得自己多么的了不起，也永远有人比你更强。",

            "梦想还是要有的，万一实现了呢？",
            "心若向阳，无谓悲伤。",
            "生活不是等待风暴过去，而是学会在雨中翩翩起舞。",
            "面朝大海，春暖花开。",
            "要有最朴素的生活和最遥远的梦想。",
            "只有经历过地狱般的折磨，才有征服天堂的力量。",
            "希望是附丽于存在的，有存在，便有希望。",
            "黑夜无论怎样悠长，白昼总会到来。",
            "既然选择了远方，便只顾风雨兼程。",
            "你的负担将变成礼物，你受的苦将照亮你的路。",
            "即使慢，驰而不息，纵会落后，纵会失败，但一定可以达到他所向往的目标。",
            "生活明朗，万物可爱。",
            "未来可期，人间值得。",
            "愿你遍历山河，觉得人间值得。",
            "心中有光，慢食三餐。",
            "保持热爱，奔赴山海。",
            "愿你以渺小启程，以伟大结束。",
            "所有失去的，都会以另一种方式归来。",
            "不要让未来的你，讨厌现在的自己。",
            "我们终其一生，就是要摆脱他人的期待，找到真正的自己。",

            "自信是成功的第一秘诀。",
            "这一秒不放弃，下一秒就有希望。",
            "人生的旅途，前途很远，也很暗。然而不要怕，不怕的人的面前才有路。",
            "勇于开始，才能找到成功的路。",
            "不要在该奋斗的年纪选择安逸。",
            "你所谓的迷茫，不过是清醒地看着自己沉沦。",
            "靠山山会倒，靠水水会流，靠自己永远不倒。",
            "哪怕遍体鳞伤，也要活得漂亮。",
            "先相信你自己，然后别人才会相信你。",
            "那些杀不死你的，终将使你更强大。",
            "真正的强者，不是没有眼泪的人，而是含着眼泪奔跑的人。",
            "不要畏惧结束，所有的结局都是一个新的开端。",
            "只有当你足够努力，你才会足够幸运。",
            "当你停下来休息的时候，别人还在奔跑。",
            "与其临渊羡鱼，不如退而结网。",
            "不要等待机会，而要创造机会。",
            "你可以平凡，但不能平庸。",
            "宁愿跑起来被拌倒无数次，也不愿规规矩矩走一辈子。",
            "如果结果不如你所愿，就在尘埃落定前奋力一搏。",
            "做你害怕做的事情，然后你会发现，不过如此。",

            "知之者不如好之者，好之者不如乐之者。",
            "学而不思则罔，思而不学则殆。",
            "人生得一知己足矣，斯世当以同怀视之。",
            "海纳百川，有容乃大；壁立千仞，无欲则刚。",
            "静以修身，俭以养德。",
            "非淡泊无以明志，非宁静无以致远。",
            "三人行，必有我师焉。",
            "君子坦荡荡，小人长戚戚。",
            "发愤忘食，乐以忘忧，不知老之将至。",
            "逝者如斯夫，不舍昼夜。",
            "天行健，君子以自强不息。",
            "地势坤，君子以厚德载物。",
            "勿以恶小而为之，勿以善小而不为。",
            "满招损，谦受益。",
            "工欲善其事，必先利其器。",
            "路漫漫其修远兮，吾将上下而求索。",
            "近朱者赤，近墨者黑。",
            "纸上得来终觉浅，绝知此事要躬行。",
            "宝剑锋从磨砺出，梅花香自苦寒来。",
            "书山有路勤为径，学海无涯苦作舟。",

            "你的时间有限，不要浪费于重复别人的生活。",
            "求知若饥，虚心若愚。",
            "复杂的事情简单做，简单的事情重复做，重复的事情用心做。",
            "格局决定结局，态度决定高度。",
            "每天叫醒你的不是闹钟，而是梦想。",
            "别低头，皇冠会掉；别流泪，坏人会笑。",
            "你努力了，成绩没有多大改观，这并不能证明你没用，毕竟你总得给运气一点时间。",
            "优秀不仅仅是一种行为，更是一种习惯。",
            "成功路上并不拥挤，因为坚持的人不多。",
            "最可怕的不是别人比你优秀，而是比你优秀的人比你更努力。",
            "你要悄悄拔尖，然后惊艳所有人。",
            "乾坤未定，你我皆是黑马。",
            "如果运气不行，那就试试勇气。",
            "生活原本沉闷，但跑起来就有风。",
            "想要得到从未得到过的东西，就要去做从未做过的事情。",
            "所谓的光辉岁月，并不是以后闪耀的日子，而是无人问津时，你对梦想的偏执。",
            "你现在的努力，是为了以后有更多的选择。",
            "种一棵树最好的时间是十年前，其次是现在。",
            "努力成为别人口中的“别人家的孩子”。",
            "不要假装努力，结果不会陪你演戏。",
            "立刻行动是治愈恐惧的良药。",
            "路虽远，行则将至；事虽难，做则必成。",
            "不要让你的梦想只停留在梦里。",
            "拖延是将易事变难的魔鬼。",
            "每一个当下，都是改变未来的起点。",
            "没有行动的梦想，终究是幻想。",
            "与其在等待中枯萎，不如在行动中绽放。",
            "最好的时机是十年前，其次是现在。",
            "再微小的努力，乘以365天，都会变得很明显。",
            "想，都是问题；做，才是答案。",
            "此时此刻，你现在的样子，是你过去的积累。",
            "不是因为看到了希望才去坚持，而是坚持了才能看到希望。",
            "平凡的脚步也可以走完伟大的行程。",
            "除了奋斗，我别无选择。",
            "哪怕每天进步一点点，也比原地踏步强。",
            "既然活着，就要活出精彩。",
            "成功的路上，没有捷径可走。",
            "你可以休息，但不能放弃。",
            "只有走出来的美丽，没有等出来的辉煌。",
            "用汗水浇灌的梦想，开花才最香。",
            "拼一个春夏秋冬，赢一个无悔人生。",
            "今天的努力，是明天的底气。",
            "懒惰是贫穷的制造厂。",
            "人生的奔跑，不在于瞬间的爆发，而在于途中的坚持。",
            "无论做什么，请记得是为你自己而做。",
            "不要为失败找借口，要为成功找方法。",
            "越努力，越幸运，这不是一句空话。",
            "如果你不逼自己一把，你永远不知道自己有多优秀。",
            "成功属于那些哪怕跌倒了无数次，依然笑着站起来的人。",
            "每一份努力，都是在为未来积蓄力量。",
            "不为模糊不清的未来担忧，只为清清楚楚的现在努力。",
            "哪怕只能看到微弱的光，也要朝着它奔跑。",
            "与其抱怨环境，不如改变自己。",
            "你要做那个在雨中奔跑的孩子，而不是躲在屋檐下的人。",
            "别让别人的眼光，挡住了你的阳光。",
            "哪怕全世界都否定你，你也要相信自己。",
            "成功的花，人们只惊羡她现时的明艳。",
            "所有的惊艳，都来自长久的准备。",
            "与其仰望星空，不如脚踏实地。",
            "每一滴汗水，都是成功的注脚。",
            "不要在奋斗的年纪选择安逸，那样你会后悔。",
            "既然目标是地平线，留给世界的只能是背影。",
            "只要路是对的，就不怕路远。",
            "你的坚持，终将美好。",
            "只有拼出来的成功，没有等出来的辉煌。",
            "哪怕是咸鱼，也要做最咸的那一条。",
            "为了未来的那个自己，现在的你必须努力。",
            "别在该吃苦的年纪选择安逸。",
            "每一次跌倒，都是为了更高地飞翔。",
            "只有自己足够强大，才能保护你想保护的人。",

            "心态决定看世界的眼光，行动决定生存的状态。",
            "心有多大，舞台就有多大。",
            "快乐不是因为拥有的多，而是因为计较的少。",
            "用微笑告诉别人，今天的我比昨天更强。",
            "如果你不能改变风向，那就调整风帆。",
            "生活是一面镜子，你对它笑，它就对你笑。",
            "真正的富有，是内心的丰盈。",
            "哪怕生活给你一地鸡毛，你也要把它扎成漂亮的鸡毛掸子。",
            "不要拿别人的错误惩罚自己。",
            "宽容别人，就是善待自己。",
            "格局打开，世界就大了。",
            "眼界决定境界，思路决定出路。",
            "做一个温暖的人，不求大富大贵，只求生活简单快乐。",
            "无论生活多么艰难，请保持一颗善良的心。",
            "真正的强大，是学会控制自己的情绪。",
            "与其纠结过去，不如拥抱未来。",
            "凡是过往，皆为序章。",
            "即使生活有一千个理由让你哭，你也要找到一个理由让自己笑。",
            "心若没有栖息的地方，到哪里都是在流浪。",
            "与其讨好别人，不如武装自己。",
            "做一个自带光芒的人，照亮自己，温暖他人。",
            "不要在该努力的时候选择抱怨。",
            "所有的烦恼，都源于能力配不上野心。",
            "真正的成熟，是看透世态炎凉，依然热爱生活。",
            "生活给你压力，你就还它奇迹。",
            "保持一颗平常心，看淡世间万物。",
            "不要因为走得太远，而忘记为什么出发。",
            "人生没有彩排，每一天都是现场直播。",
            "你的善良，必须带点锋芒。",
            "别让糟糕的情绪，毁了美好的一天。",
            "做一个积极向上的人，读温柔的句子，见阳光的人。",
            "把脸迎向阳光，你的面前就不会有阴影。",
            "不要在小事上斤斤计较，要在大事上懂得变通。",
            "学会独处，是成长的必修课。",
            "不要活在别人的嘴里，也不要活在别人的眼里。",
            "只有内心强大，才能无惧风雨。",
            "与其羡慕别人的花园，不如种好自己的花朵。",
            "人生最大的失败，就是放弃。",
            "只要心中有爱，世界就会变得美好。",
            "保持热爱，生活才会变得有趣。",
            "学会感恩，生活会回馈你更多。",
            "真正的勇敢，是认清生活的真相后依然热爱它。",
            "不要轻言放弃，否则对不起自己。",
            "做一个灵魂有香气的人。",
            "只要心是晴朗的，人生就没有雨天。",
            "生活不只眼前的苟且，还有诗和远方。",
            "做一个内心丰富的人，不惧孤独。",
            "只有学会放下，才能轻松前行。",
            "保持好奇心，是青春常驻的秘诀。",
            "愿你眼里有光，心中有爱，一路春暖花开。",

            "读书，是门槛最低的高贵。",
            "腹有诗书气自华。",
            "读万卷书，行万里路。",
            "书籍是人类进步的阶梯。",
            "知识改变命运，学习成就未来。",
            "鸟欲高飞先振翅，人求上进先读书。",
            "一日不书，百事荒芜。",
            "黑发不知勤学早，白首方悔读书迟。",
            "立身以立学为先，立学以读书为本。",
            "书犹药也，善读之可以医愚。",
            "读书破万卷，下笔如有神。",
            "问渠那得清如许，为有源头活水来。",
            "旧书不厌百回读，熟读深思子自知。",
            "读书百遍，其义自见。",
            "学无止境，气有浩然。",
            "才须学也，非学无以广才。",
            "少壮不努力，老大徒伤悲。",
            "莫等闲，白了少年头，空悲切。",
            "三更灯火五更鸡，正是男儿读书时。",
            "粗缯大布裹生涯，腹有诗书气自华。",
            "书中自有黄金屋，书中自有颜如玉。",
            "人之所以能，是相信能。",
            "只要功夫深，铁杵磨成针。",
            "世上无难事，只怕有心人。",
            "学如逆水行舟，不进则退。",
            "温故而知新，可以为师矣。",
            "在这个浮躁的时代，读书能让你静下来。",
            "读书，是为了遇见更好的自己。",
            "你读过的书，藏着你的气质和谈吐。",
            "知识是唯一别人抢不走的财富。",
            "不要把学习当成负担，它是你飞翔的翅膀。",
            "每天阅读一小时，坚持下去，你会感谢自己。",
            "读书让你看到更大的世界，而不是困在眼前。",
            "只有不断学习，才不会被时代抛弃。",
            "学习是抵御平庸的最好武器。",
            "用知识武装头脑，比用名牌包装外表更重要。",
            "与其沉迷网络，不如静心读书。",
            "读书能让你的灵魂变得有趣。",
            "通过阅读，你可以和伟大的灵魂对话。",
            "学习永远不晚，只要你开始。",
            "读书是一种生活方式，而不只是任务。",
            "在书中寻找答案，在生活中寻找真理。",
            "知识就是力量，智慧就是财富。",
            "让阅读成为习惯，让思考成为常态。",
            "读书，是为了让你有更多选择的权利。",
            "不要因为忙碌而停止学习。",
            "活到老，学到老，还有三分学不到。",
            "知识是通往自由的钥匙。",
            "只有思想独立，人格才能独立。",
            "读书，是给灵魂洗澡。",

            "人生没有白走的路，每一步都算数。",
            "不要因为错过太阳而哭泣，那样你也会错过群星。",
            "只有经历过岁月的洗礼，才能沉淀出人生的智慧。",
            "时间是公平的，它给每个人都是24小时。",
            "珍惜当下，因为它是你余生中最年轻的一刻。",
            "不要预支明天的烦恼。",
            "只有学会和自己握手言和，才能获得真正的安宁。",
            "人生就像一场马拉松，笑到最后才是赢家。",
            "有些路，只能一个人走。",
            "不要为了合群而丢失了自己。",
            "孤独是强者的必修课。",
            "只有耐得住寂寞，才能守得住繁华。",
            "人生不如意事十之八九，常想一二。",
            "在这个世界上，唯一不变的就是变化。",
            "不要把希望寄托在别人身上，那叫赌博。",
            "人生最大的财富是健康。",
            "不要因为一时的得失而乱了方寸。",
            "只有经历过风雨，才能见到彩虹。",
            "人生是一场修行，修的是心。",
            "不要让过去的阴影，遮挡了未来的阳光。",
            "学会放下，才能拥有更多。",
            "人生没有如果，只有结果和后果。",
            "不要为了取悦别人而委屈自己。",
            "每个人都是自己人生的导演。",
            "与其抱怨命运不公，不如努力改变命运。",
            "人生没有彩排，每天都是现场直播。",
            "只有懂得珍惜，才配拥有。",
            "不要把坏情绪带给亲近的人。",
            "做人要像茶壶，屁股都烧红了，还有心情吹口哨。",
            "人生就是一场体验，尽兴就好。",
            "不要因为别人的评价而否定自己。",
            "只有内心安宁，才能听到花开的声音。",
            "简单生活，简单爱。",
            "人生最大的幸福，是做自己喜欢的事。",
            "不要让欲望吞噬了你的灵魂。",
            "知足常乐，是幸福的源泉。",
            "人生苦短，不要给自己留遗憾。",
            "每一个清晨，都是重生的机会。",
            "不要等到失去了才懂得珍惜。",
            "只有经历过离别，才懂得相聚的可贵。",
            "人生如戏，全靠演技，但要做真实的自己。",
            "不要因为走得太快，而忘了欣赏沿途的风景。",
            "人生是一场单程旅行，没有回程票。",
            "做人要厚道，做事要地道。",
            "不要为了名利而迷失了方向。",
            "只有心怀感恩，才能遇见美好。",
            "人生最大的敌人是自己。",
            "战胜自己，就是最大的胜利。",
            "愿你出走半生，归来仍是少年。",
            "岁月静好，现世安稳。"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_list);

        dbHelper = new DatabaseHelper(this);

        tvDate = findViewById(R.id.tvDate);
        settingBtn = findViewById(R.id.btnSettings);
        btnDeleteCompleted = findViewById(R.id.btnDeleteCompleted);

        titleTextView = findViewById(R.id.tvTitle);
        tvQuote = findViewById(R.id.tvQuote);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddTask);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        settingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PocoListActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                    if (appPrefs.getBoolean("needs_recreate", false)) {
                        appPrefs.edit().putBoolean("needs_recreate", false).apply();
                        recreate();
                    }
                }
        );


        if (btnDeleteCompleted != null) {
            btnDeleteCompleted.setOnClickListener(v -> showDeleteConfirmDialog());
        }

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        setupHeader();
        setupTabs();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkQuoteVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 这里就是停止一下这个接口
        if (quoteHandler != null) {
            quoteHandler.removeCallbacks(quoteRunnable);
        }
    }


    private void checkQuoteVisibility() {
        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isQuoteEnabled = appPrefs.getBoolean("quote_enabled", true);

        if (tvQuote != null) {
            if (isQuoteEnabled) {
                tvQuote.setVisibility(android.view.View.VISIBLE);
                if (quoteHandler != null) {
                        quoteHandler.removeCallbacks(quoteRunnable);
                    startQuoteRotation();
                }
            } else {
                tvQuote.setVisibility(android.view.View.GONE);
                if (quoteHandler != null) {
                    quoteHandler.removeCallbacks(quoteRunnable);
                }
            }
        }
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("批量删除");
        builder.setMessage("准备好迎接新的开始了吗?");

        builder.setPositiveButton("清理", (dialog, which) -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentUser = prefs.getString("current_user", "Guest");

            dbHelper.deleteCompletedTasks(currentUser);

            int currentItem = viewPager.getCurrentItem();
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setCurrentItem(currentItem, false);

            Toast.makeText(this, "已清理所有已完成任务", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", null);
        showStyledDialog(builder);
    }

    private void setupHeader() {
        String user = getIntent().getStringExtra("USER_NAME");
        if (user == null) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            user = prefs.getString("current_user", "My");
        }
        titleTextView.setText(user + "  's Tasks");

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        tvDate.setText(currentDate.toUpperCase());
    }

    private void setupTabs() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(TAB_TITLES[position])
        ).attach();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return TaskFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int currentTab = viewPager.getCurrentItem();
        String typeName = TAB_TITLES[currentTab];
        builder.setTitle("新增 " + typeName);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("要干啥 伙计?");
        layout.addView(etTitle);

        final TextView tvTimePick = new TextView(this);
        final StringBuilder finalDateTime = new StringBuilder();
        final android.widget.Spinner spinnerSeason = new android.widget.Spinner(this);

        if (currentTab == 0 || currentTab == 1) {
            tvTimePick.setText(currentTab == 0 ? "ddl  🕑 (点击设置)" : "选择日期 📅");
            tvTimePick.setPadding(0, 30, 0, 20);
            tvTimePick.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            layout.addView(tvTimePick);
            tvTimePick.setOnClickListener(v -> {
                if (currentTab == 0) {
                    pickDateTime(tvTimePick, finalDateTime);
                } else {
                    pickDateOnly(tvTimePick, finalDateTime);
                }
            });
        } else if (currentTab == 2) {
            String[] seasons = {"🌸 春季", "🍉 夏季", "🍁 秋季", "❄️ 冬季"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, seasons
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSeason.setAdapter(adapter);
            TextView tvSeasonLabel = new TextView(this);
            tvSeasonLabel.setText("选择季节:");
            layout.addView(tvSeasonLabel);
            layout.addView(spinnerSeason);
        }

        builder.setView(layout);

        builder.setPositiveButton("就这个了", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) return;

            String dateTime = "";
            if (currentTab == 0 || currentTab == 1) {
                dateTime = finalDateTime.toString();
            } else if (currentTab == 2) {
                String selectedSeason = spinnerSeason.getSelectedItem().toString();
                if (selectedSeason.contains("春")) title = "[春季] " + title;
                else if (selectedSeason.contains("夏")) title = "[夏季] " + title;
                else if (selectedSeason.contains("秋")) title = "[秋季] " + title;
                else if (selectedSeason.contains("冬")) title = "[冬季] " + title;
            }

            Task newTask = new Task(title, false, dateTime);
            newTask.setTaskType(currentTab);

            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentUser = prefs.getString("current_user", "Guest");
            dbHelper.addTask(newTask, currentUser);

            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setCurrentItem(currentTab, false);

            if (currentTab == 0 && !dateTime.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateTime);
                    if (date != null && date.getTime() > System.currentTimeMillis()) {
                        scheduleNotification(title, date.getTime());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("算鸟算鸟", (dialog, which) -> dialog.cancel());
        showStyledDialog(builder);
    }

    public void pickDateTime(TextView displayView, StringBuilder outputString) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                String result = dateStr + " " + timeStr;

                displayView.setText(result);
                displayView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

                outputString.setLength(0);
                outputString.append(result);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void pickDateOnly(TextView displayView, StringBuilder outputString) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String result = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
            displayView.setText(result);
            displayView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            outputString.setLength(0);
            outputString.append(result);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStyledDialog(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }

    public void scheduleNotification(String title, long deadlineInMillis) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int offsetMinutes = prefs.getInt("reminder_offset", 0);
        long offsetMillis = offsetMinutes * 60 * 1000L;

        long triggerTime = deadlineInMillis - offsetMillis;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "请授予“闹钟和提醒”权限以接收通知", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        }

        if (triggerTime > System.currentTimeMillis()) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            if (offsetMinutes > 0) {
                intent.putExtra("TASK_TITLE", title + " (还有 " + offsetMinutes + " 分钟)");
            } else {
                intent.putExtra("TASK_TITLE", title);
            }

            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            if (alarmManager != null) {
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    String toastMsg = "提醒已设置";
                    if (offsetMinutes > 0) {
                        toastMsg += " (将提前 " + offsetMinutes + " 分钟通知)";
                    }
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "权限不足，无法设置提醒", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startQuoteRotation() {
        if (tvQuote.getVisibility() != android.view.View.VISIBLE) return;

        quoteRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvQuote.getVisibility() == android.view.View.VISIBLE) {
                    updateQuoteWithAnimation();
                    quoteHandler.postDelayed(this, 4000);
                }
            }
        };
        quoteHandler.post(quoteRunnable);
    }

    private void updateQuoteWithAnimation() {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setFillAfter(true);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        fadeIn.setFillAfter(true);

        tvQuote.startAnimation(fadeOut);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuoteIndex = (int) (Math.random() * quotes.length);
            tvQuote.setText(quotes[currentQuoteIndex]);
            tvQuote.startAnimation(fadeIn);
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (quoteHandler != null && quoteRunnable != null) {
            quoteHandler.removeCallbacks(quoteRunnable);
        }
    }
}
