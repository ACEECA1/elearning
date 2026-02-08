$(document).ready(function() {

    var sidebar = $('#sidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche : " + texte);
            }
        }
    });

    var videoEnLecture = false;
    var timer;
    var progression = 30;

    function lancerVideo() {
        videoEnLecture = true;
        $('#playButtonLarge').hide();
        $('#playPauseBtn i').removeClass('fa-play');
        $('#playPauseBtn i').addClass('fa-pause');
        
        timer = setInterval(function() {
            progression++;
            if (progression > 100) progression = 0;
            $('#progressFilled').css('width', progression + '%');
        }, 500);
    }

    function stopperVideo() {
        videoEnLecture = false;
        $('#playButtonLarge').show();
        $('#playPauseBtn i').removeClass('fa-pause');
        $('#playPauseBtn i').addClass('fa-play');
        clearInterval(timer);
    }

    $('#playButtonLarge').click(function() {
        lancerVideo();
    });

    $('#playPauseBtn').click(function() {
        if (videoEnLecture) {
            stopperVideo();
        } else {
            lancerVideo();
        }
    });

    $('#volumeBtn').click(function() {
        alert("Volume modifié");
    });

    $('#settingsBtn').click(function() {
        alert("Paramètres vidéo");
    });

    $('#fullscreenBtn').click(function() {
        alert("Plein écran");
    });

    $('.tab-btn').click(function() {
        $('.tab-btn').removeClass('active');
        $(this).addClass('active');
        
        var onglet = $(this).attr('data-tab');
        if (onglet == 'discussion') {
            $('#discussionSection').show();
        } else {
            $('#discussionSection').hide();
            alert("Affichage de l'onglet : " + onglet);
        }
    });

    $('#sendCommentBtn').click(function() {
        var input = $('#commentInput');
        var texte = input.val();

        if (texte != "") {
            var nouveauCommentaire = '<div class="comment-item">' +
                '<div class="comment-avatar-img" style="background:#ddd; border-radius:50%; width:40px; height:40px; text-align:center; line-height:40px;">Me</div>' +
                '<div class="comment-content">' +
                '<div class="comment-header"><span class="comment-author">Alex Johnson</span> <span class="comment-time">À l\'instant</span></div>' +
                '<p class="comment-text">' + texte + '</p>' +
                '</div></div>';
            
            $('.comments-list').prepend(nouveauCommentaire);
            input.val("");
        }
    });

    $('.like-btn').click(function() {
        var btn = $(this);
        var compteur = btn.find('.like-count');
        var nombre = parseInt(compteur.text());

        if (btn.hasClass('liked')) {
            btn.removeClass('liked');
            btn.css('color', '');
            nombre = nombre - 1;
        } else {
            btn.addClass('liked');
            btn.css('color', '#4a3bbf');
            nombre = nombre + 1;
        }
        compteur.text(nombre);
    });

    $('.reply-btn').click(function() {
        alert("Répondre au commentaire");
    });

    console.log("Page de discussion chargée");
});