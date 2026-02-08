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

    $('#openModalBtn').click(function() {
        $('#videoModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#videoModal').fadeOut();
    });

    $('#selectVideoBtn').click(function() {
        $('#videoFileInput').click();
    });

    $('#videoFileInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Vidéo sélectionnée : " + fichier);
            $('.upload-title').text("Vidéo chargée");
            $('.upload-subtitle').text(fichier);
        }
    });

    $('#uploadArea').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('border-color', '#4a3bbf');
    }, function() {
        $(this).css('background-color', '');
        $(this).css('border-color', '');
    });

    $('#videoContentForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#videoTitle').val();
        var fichier = $('#videoFileInput').val();

        if (titre == "") {
            alert("Veuillez entrer un titre pour la vidéo.");
        } else if (fichier == "") {
            alert("Veuillez sélectionner un fichier vidéo.");
        } else {
            alert("La vidéo '" + titre + "' a été ajoutée avec succès.");
            $('#videoModal').fadeOut();
            $('#videoTitle').val("");
            $('.upload-title').text("Upload Video");
            $('.upload-subtitle').text("MP4, WebM or Ogg (Max 2GB)");
        }
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Page d'édition vidéo chargée");
});