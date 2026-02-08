$(document).ready(function() {

    var toggleBtn = $('#mobileSidebarToggle');
    var sidebar = $('#adminSidebar');

    toggleBtn.click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    var notificationBtn = $('#notificationBtn');

    notificationBtn.click(function() {
        var btn = $(this);
        
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) { 
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche en cours : " + texte);
            }
        }
    });

    function animerChiffres() {
        $('.stat-value').each(function() {
            var element = $(this);
            var texteOrigine = element.text();
            
            var valeurFinale = parseInt(texteOrigine.replace(/,/g, ''));
            
            var courant = 0;
            var pas = Math.ceil(valeurFinale / 50); 

            var timer = setInterval(function() {
                courant = courant + pas;

                if (courant >= valeurFinale) {
                    courant = valeurFinale;
                    clearInterval(timer);
                }

                element.text(courant);
            }, 30);
        });
    }

    animerChiffres();

    $('.admin-menu-item a').click(function() {
        $('.admin-menu-item a').removeClass('active');
        $(this).addClass('active');
    });

    $('.user-profile').click(function() {
        alert("Profil administrateur : Alex Johnson");
    });

    console.log("Tableau de bord chargé avec succès");
});