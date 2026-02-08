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

    var coursASupprimer = null;

    $('.delete-btn').click(function() {
        coursASupprimer = $(this).parents('.course-card');
        $('#deleteModal').fadeIn();
    });

    $('#modalClose, #cancelDelete').click(function() {
        $('#deleteModal').fadeOut();
        coursASupprimer = null;
    });

    $('#confirmDelete').click(function() {
        if (coursASupprimer) {
            coursASupprimer.fadeOut(function() {
                $(this).remove();
            });
            alert("Le cours a été supprimé.");
        }
        $('#deleteModal').fadeOut();
    });

    $('.edit-btn').click(function() {
        var carte = $(this).parents('.course-card');
        var titre = carte.find('.course-card-title').text();
        alert("Édition du cours : " + titre);
    });

    $('.view-course-btn').click(function() {
        var carte = $(this).parents('.course-card');
        var titre = carte.find('.course-card-title').text();
        alert("Aperçu du cours : " + titre);
    });

    $('.course-info-btn').click(function() {
        alert("Détails rapides du cours affichés.");
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Tableau de bord enseignant chargé");
});