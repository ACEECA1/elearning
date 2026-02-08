$(document).ready(function() {

    var sidebar = $('#adminSidebar');
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
            var val = $(this).val();
            if (val != "") {
                alert("Recherche globale : " + val);
            }
        }
    });

    $('#courseSearch').keyup(function() {
        var valeur = $(this).val().toLowerCase();
        
        $('.course-row').each(function() {
            var ligne = $(this);
            var texte = ligne.text().toLowerCase();
            
            if (texte.indexOf(valeur) > -1) {
                ligne.show();
            } else {
                ligne.hide();
            }
        });
    });

    $('#filterBtn').click(function() {
        alert("Fonctionnalité de filtrage");
    });

    $('.delete-btn').click(function() {
        var confirmation = confirm("Voulez-vous vraiment supprimer ce cours ?");
        
        if (confirmation) {
            $(this).parents('tr').remove();
        }
    });

    $('.edit-btn').click(function() {
        var ligne = $(this).parents('tr');
        var titre = ligne.find('.course-name').text();
        alert("Modification du cours : " + titre);
    });

    console.log("Page des cours chargée");
});