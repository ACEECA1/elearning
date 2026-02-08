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

    $('#backBtn').click(function() {
        alert("Retour au tableau de bord");
    });

    $('.edit-btn').click(function() {
        var carte = $(this).parents('.chapter-card');
        var titre = carte.find('.chapter-title').text();
        alert("Modification du chapitre : " + titre);
    });

    $('.delete-btn').click(function() {
        var carte = $(this).parents('.chapter-card');
        var titre = carte.find('.chapter-title').text();
        
        var confirmation = confirm("Voulez-vous vraiment supprimer le chapitre : " + titre + " ?");
        
        if (confirmation) {
            carte.fadeOut(function() {
                $(this).remove();
            });
        }
    });

    $('.content-item').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('cursor', 'pointer');
    }, function() {
        $(this).css('background-color', '');
    });

    $('.content-item').click(function() {
        var contenu = $(this).find('.content-title').text();
        alert("Aperçu du contenu : " + contenu);
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Gestion du contenu du module chargée");
});